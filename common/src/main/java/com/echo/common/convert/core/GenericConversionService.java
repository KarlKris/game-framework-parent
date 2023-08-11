package com.echo.common.convert.core;

import com.echo.common.concurrency.ConcurrentReferenceHashMap;
import com.echo.common.convert.converter.ConditionalConverter;
import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.converter.GenericConverter;
import com.echo.common.convert.exception.ConversionFailedException;
import com.echo.common.convert.exception.ConverterNotFoundException;
import com.echo.common.util.ClassUtils;
import com.echo.common.util.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ConfigurableConversionService 完整实现
 * 特别强调：转换器的注册顺序非常重要，这决定了通用转换器的匹配结果（谁在前，优先匹配谁，first win）。
 * @author: li-yuanwen
 */
public class GenericConversionService implements ConfigurableConversionService {

    /**
     * 默认转换器
     */
    private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter("NO_OP");

    /**
     * 找不到转换器时缓存占坑
     */
    private static final GenericConverter NO_MATCH = new NoOpConverter("NO_MATCH");


    private final Converters converters = new Converters();

    /** 缓存 **/
    private final Map<ConverterCacheKey, GenericConverter> converterCache = new ConcurrentReferenceHashMap<>(64);

    // -------------------- ConverterRegistry ---------------------------------

    @Override
    public void addConverter(Converter<?, ?> converter) {
        ResolvableType[] typeInfo = getRequiredTypeInfo(converter.getClass(), Converter.class);
        if (typeInfo == null) {
            throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
                    "Converter [" + converter.getClass().getName() + "]; does the class parameterize those types?");
        }
        addConverter(new ConverterAdapter(converter, typeInfo[0], typeInfo[1]));
    }

    @Override
    public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter) {
        addConverter(new ConverterAdapter(
                converter, ResolvableType.forClass(sourceType), ResolvableType.forClass(targetType)));
    }

    @Override
    public void addConverter(GenericConverter converter) {
        converters.add(converter);
        invalidateCache();
    }

    @Override
    public void addConverterFactory(ConverterFactory<?, ?> factory) {
        ResolvableType[] typeInfo = getRequiredTypeInfo(factory.getClass(), ConverterFactory.class);
        if (typeInfo == null) {
            throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
                    "ConverterFactory [" + factory.getClass().getName() + "]; does the class parameterize those types?");
        }
        addConverter(new ConverterFactoryAdapter(factory,
                new GenericConverter.ConvertiblePair(typeInfo[0].toClass(), typeInfo[1].toClass())));
    }

    @Override
    public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
        this.converters.remove(sourceType, targetType);
        invalidateCache();
    }

    // -------------------- ConversionService ---------------------------------

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type to convert to cannot be null");
        }
        return canConvert((sourceType != null ? TypeDescriptor.valueOf(sourceType) : null),
                TypeDescriptor.valueOf(targetType));
    }

    @Override
    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type to convert to cannot be null");
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        return (converter != null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T convert(Object source, Class<T> targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type to convert to cannot be null");
        }
        return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type to convert to cannot be null");
        }
        if (sourceType == null) {
            if (source != null) {
                throw new IllegalArgumentException("Source must be [null] if source type == [null]");
            }
            return handleResult(null, targetType, convertNullSource(null, targetType));
        }
        if (source != null && !sourceType.getObjectType().isInstance(source)) {
            throw new IllegalArgumentException("Source to convert from must be an instance of [" +
                    sourceType + "]; instead it was a [" + source.getClass().getName() + "]");
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        if (converter != null) {
            Object result = invokeConverter(converter, source, sourceType, targetType);
            return handleResult(sourceType, targetType, result);
        }
        return handleConverterNotFound(source, sourceType, targetType);
    }


    /**
     * Return whether conversion between the source type and the target type can be bypassed.
     * <p>More precisely, this method will return true if objects of sourceType can be
     * converted to the target type by returning the source object unchanged.
     * @param sourceType context about the source type to convert from
     * (may be {@code null} if source is {@code null})
     * @param targetType context about the target type to convert to (required)
     * @return {@code true} if conversion can be bypassed; {@code false} otherwise
     * @throws IllegalArgumentException if targetType is {@code null}
     */
    public boolean canBypassConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type to convert to cannot be null");
        }
        if (sourceType == null) {
            return true;
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        return converter == NO_OP_CONVERTER;
    }

    // -----------------------------------------------------------------------------------

    protected Object convertNullSource(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType.getObjectType() == Optional.class) {
            return Optional.empty();
        }
        return null;
    }

    protected GenericConverter getDefaultConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return (sourceType.isAssignableTo(targetType) ? NO_OP_CONVERTER : null);
    }

    protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
        ConverterCacheKey key = new ConverterCacheKey(sourceType, targetType);
        GenericConverter converter = this.converterCache.get(key);
        if (converter != null) {
            return (converter != NO_MATCH ? converter : null);
        }

        converter = this.converters.find(sourceType, targetType);
        if (converter == null) {
            converter = getDefaultConverter(sourceType, targetType);
        }

        if (converter != null) {
            this.converterCache.put(key, converter);
            return converter;
        }

        this.converterCache.put(key, NO_MATCH);
        return null;
    }

    private ResolvableType[] getRequiredTypeInfo(Class<?> converterClass, Class<?> genericIfc) {
        ResolvableType resolvableType = ResolvableType.forClass(converterClass).as(genericIfc);
        ResolvableType[] generics = resolvableType.getGenerics();
        if (generics.length < 2) {
            return null;
        }
        Class<?> sourceType = generics[0].resolve();
        Class<?> targetType = generics[1].resolve();
        if (sourceType == null || targetType == null) {
            return null;
        }
        return generics;
    }

    private void invalidateCache() {
        this.converterCache.clear();
    }

    private Object handleResult(TypeDescriptor sourceType, TypeDescriptor targetType, Object result) {
        if (result == null) {
            assertNotPrimitiveTargetType(sourceType, targetType);
        }
        return result;
    }

    private Object handleConverterNotFound(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        if (source == null) {
            assertNotPrimitiveTargetType(sourceType, targetType);
            return null;
        }
        if ((sourceType == null || sourceType.isAssignableTo(targetType)) &&
                targetType.getObjectType().isInstance(source)) {
            return source;
        }
        throw new ConverterNotFoundException(sourceType, targetType);
    }

    private void assertNotPrimitiveTargetType(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType.isPrimitive()) {
            throw new ConversionFailedException(sourceType, targetType, null,
                    new IllegalArgumentException("A null value cannot be assigned to a primitive type"));
        }
    }

    private Object invokeConverter(GenericConverter converter, Object source,
                                   TypeDescriptor sourceType, TypeDescriptor targetType) {
        try {
            return converter.convert(source, sourceType, targetType);
        }
        catch (ConversionFailedException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new ConversionFailedException(sourceType, targetType, source, ex);
        }
    }

    // ---------------------------------------------------------------------------------------------------

    /**
     * Adapts a {@link Converter} to a {@link GenericConverter}.
     */
    @SuppressWarnings("unchecked")
    private final class ConverterAdapter implements ConditionalGenericConverter {

        private final Converter<Object, Object> converter;

        private final ConvertiblePair typeInfo;

        private final ResolvableType targetType;

        public ConverterAdapter(Converter<?, ?> converter, ResolvableType sourceType, ResolvableType targetType) {
            this.converter = (Converter<Object, Object>) converter;
            this.typeInfo = new ConvertiblePair(sourceType.toClass(), targetType.toClass());
            this.targetType = targetType;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(this.typeInfo);
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            // Check raw type first...
            if (this.typeInfo.getTargetType() != targetType.getObjectType()) {
                return false;
            }
            // Full check for complex generic type match required?
            ResolvableType rt = targetType.getResolvableType();
            if (!(rt.getType() instanceof Class) && !rt.isAssignableFrom(this.targetType) &&
                    !this.targetType.hasUnresolvableGenerics()) {
                return false;
            }
            return !(this.converter instanceof ConditionalConverter) ||
                    ((ConditionalConverter) this.converter).matches(sourceType, targetType);
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return convertNullSource(sourceType, targetType);
            }
            return this.converter.convert(source);
        }

        @Override
        public String toString() {
            return (this.typeInfo + " : " + this.converter);
        }
    }


    /**
     * Adapts a {@link ConverterFactory} to a {@link GenericConverter}.
     */
    @SuppressWarnings("unchecked")
    private final class ConverterFactoryAdapter implements ConditionalGenericConverter {

        private final ConverterFactory<Object, Object> converterFactory;

        private final ConvertiblePair typeInfo;

        public ConverterFactoryAdapter(ConverterFactory<?, ?> converterFactory, ConvertiblePair typeInfo) {
            this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
            this.typeInfo = typeInfo;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(this.typeInfo);
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            boolean matches = true;
            if (this.converterFactory instanceof ConditionalConverter) {
                matches = ((ConditionalConverter) this.converterFactory).matches(sourceType, targetType);
            }
            if (matches) {
                Converter<?, ?> converter = this.converterFactory.getConverter(targetType.getType());
                if (converter instanceof ConditionalConverter) {
                    matches = ((ConditionalConverter) converter).matches(sourceType, targetType);
                }
            }
            return matches;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return convertNullSource(sourceType, targetType);
            }
            return this.converterFactory.getConverter(targetType.getObjectType()).convert(source);
        }

        @Override
        public String toString() {
            return (this.typeInfo + " : " + this.converterFactory);
        }
    }


    /**
     * Key for use with the converter cache.
     */
    private static final class ConverterCacheKey implements Comparable<ConverterCacheKey> {

        private final TypeDescriptor sourceType;

        private final TypeDescriptor targetType;

        public ConverterCacheKey(TypeDescriptor sourceType, TypeDescriptor targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ConverterCacheKey)) {
                return false;
            }
            ConverterCacheKey otherKey = (ConverterCacheKey) other;
            return (this.sourceType.equals(otherKey.sourceType)) &&
                    this.targetType.equals(otherKey.targetType);
        }

        @Override
        public int hashCode() {
            return (this.sourceType.hashCode() * 29 + this.targetType.hashCode());
        }

        @Override
        public String toString() {
            return ("ConverterCacheKey [sourceType = " + this.sourceType +
                    ", targetType = " + this.targetType + "]");
        }

        @Override
        public int compareTo(ConverterCacheKey other) {
            int result = this.sourceType.getResolvableType().toString().compareTo(
                    other.sourceType.getResolvableType().toString());
            if (result == 0) {
                result = this.targetType.getResolvableType().toString().compareTo(
                        other.targetType.getResolvableType().toString());
            }
            return result;
        }
    }


    /**
     * Manages all converters registered with the service.
     */
    private static class Converters {

        /** 存取通用的转换器，并不限定转换类型，一般用于兜底 **/
        private final Set<GenericConverter> globalConverters = new CopyOnWriteArraySet<>();

        /** 指定了类型对，对应的转换器们的映射关系。 **/
        private final Map<GenericConverter.ConvertiblePair, ConvertersForPair> converters = new ConcurrentHashMap<>(256);

        public void add(GenericConverter converter) {
            Set<GenericConverter.ConvertiblePair> convertibleTypes = converter.getConvertibleTypes();
            if (convertibleTypes == null) {
                if (!(converter instanceof ConditionalConverter)) {
                    throw new IllegalStateException("Only conditional converters may return null convertible types");
                }
                this.globalConverters.add(converter);
            }
            else {
                for (GenericConverter.ConvertiblePair convertiblePair : convertibleTypes) {
                    getMatchableConverters(convertiblePair).add(converter);
                }
            }
        }

        private ConvertersForPair getMatchableConverters(GenericConverter.ConvertiblePair convertiblePair) {
            return this.converters.computeIfAbsent(convertiblePair, k -> new ConvertersForPair());
        }

        public void remove(Class<?> sourceType, Class<?> targetType) {
            this.converters.remove(new GenericConverter.ConvertiblePair(sourceType, targetType));
        }

        /**
         * Find a {@link GenericConverter} given a source and target type.
         * <p>This method will attempt to match all possible converters by working
         * through the class and interface hierarchy of the types.
         * @param sourceType the source type
         * @param targetType the target type
         * @return a matching {@link GenericConverter}, or {@code null} if none found
         */
        public GenericConverter find(TypeDescriptor sourceType, TypeDescriptor targetType) {
            // 找到该类型的类层次接口（父类 + 接口），注意：结果是有序列表
            List<Class<?>> sourceCandidates = getClassHierarchy(sourceType.getType());
            List<Class<?>> targetCandidates = getClassHierarchy(targetType.getType());
            for (Class<?> sourceCandidate : sourceCandidates) {
                for (Class<?> targetCandidate : targetCandidates) {
                    // 从converters、globalConverters里匹配到一个合适转换器后立马返回
                    GenericConverter.ConvertiblePair convertiblePair = new GenericConverter.ConvertiblePair(sourceCandidate, targetCandidate);
                    GenericConverter converter = getRegisteredConverter(sourceType, targetType, convertiblePair);
                    if (converter != null) {
                        return converter;
                    }
                }
            }
            return null;
        }

        private GenericConverter getRegisteredConverter(TypeDescriptor sourceType,
                                                        TypeDescriptor targetType, GenericConverter.ConvertiblePair convertiblePair) {

            // Check specifically registered converters
            ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
            if (convertersForPair != null) {
                GenericConverter converter = convertersForPair.getConverter(sourceType, targetType);
                if (converter != null) {
                    return converter;
                }
            }
            // Check ConditionalConverters for a dynamic match
            for (GenericConverter globalConverter : this.globalConverters) {
                if (!(globalConverter instanceof ConditionalConverter)) {
                    continue;
                }
                if (((ConditionalConverter) globalConverter).matches(sourceType, targetType)) {
                    return globalConverter;
                }
            }
            return null;
        }

        /**
         * Returns an ordered class hierarchy for the given type.
         * @param type the type
         * @return an ordered list of all classes that the given type extends or implements
         */
        private List<Class<?>> getClassHierarchy(Class<?> type) {
            List<Class<?>> hierarchy = new ArrayList<>(20);
            Set<Class<?>> visited = new HashSet<>(20);
            addToClassHierarchy(0, ClassUtils.resolvePrimitiveIfNecessary(type), false, hierarchy, visited);
            boolean array = type.isArray();

            int i = 0;
            while (i < hierarchy.size()) {
                Class<?> candidate = hierarchy.get(i);
                candidate = (array ? candidate.getComponentType() : ClassUtils.resolvePrimitiveIfNecessary(candidate));
                Class<?> superclass = candidate.getSuperclass();
                if (superclass != null && superclass != Object.class && superclass != Enum.class) {
                    addToClassHierarchy(i + 1, candidate.getSuperclass(), array, hierarchy, visited);
                }
                addInterfacesToClassHierarchy(candidate, array, hierarchy, visited);
                i++;
            }

            if (Enum.class.isAssignableFrom(type)) {
                addToClassHierarchy(hierarchy.size(), Enum.class, array, hierarchy, visited);
                addToClassHierarchy(hierarchy.size(), Enum.class, false, hierarchy, visited);
                addInterfacesToClassHierarchy(Enum.class, array, hierarchy, visited);
            }

            addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
            addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
            return hierarchy;
        }

        private void addInterfacesToClassHierarchy(Class<?> type, boolean asArray,
                                                   List<Class<?>> hierarchy, Set<Class<?>> visited) {

            for (Class<?> implementedInterface : type.getInterfaces()) {
                addToClassHierarchy(hierarchy.size(), implementedInterface, asArray, hierarchy, visited);
            }
        }

        private void addToClassHierarchy(int index, Class<?> type, boolean asArray,
                                         List<Class<?>> hierarchy, Set<Class<?>> visited) {

            if (asArray) {
                type = Array.newInstance(type, 0).getClass();
            }
            if (visited.add(type)) {
                hierarchy.add(index, type);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConversionService converters =\n");
            for (String converterString : getConverterStrings()) {
                builder.append('\t').append(converterString).append('\n');
            }
            return builder.toString();
        }

        private List<String> getConverterStrings() {
            List<String> converterStrings = new ArrayList<>();
            for (ConvertersForPair convertersForPair : this.converters.values()) {
                converterStrings.add(convertersForPair.toString());
            }
            Collections.sort(converterStrings);
            return converterStrings;
        }
    }


    /**
     * 这一对对应的转换器们（因为能处理一对的可能存在多个转换器），内部使用一个双端队列Deque来存储，保证顺序 {@link ConvertersForPair}
     */
    private static class ConvertersForPair {

        private final Deque<GenericConverter> converters = new ConcurrentLinkedDeque<>();

        public void add(GenericConverter converter) {
            this.converters.addFirst(converter);
        }

        public GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
            for (GenericConverter converter : this.converters) {
                if (!(converter instanceof ConditionalGenericConverter) ||
                        ((ConditionalGenericConverter) converter).matches(sourceType, targetType)) {
                    return converter;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return StringUtils.collectionToCommaDelimitedString(this.converters);
        }
    }


    /**
     * 不做任何操作的转换器
     */
    private static class NoOpConverter implements GenericConverter {

        private final String name;

        public NoOpConverter(String name) {
            this.name = name;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return source;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
