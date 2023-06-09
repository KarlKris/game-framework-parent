package com.li.common.conversion;

import cn.hutool.core.lang.Assert;
import com.li.common.conversion.converter.Converter;
import com.sun.istack.internal.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 转换器服务
 */
public class ConversionService implements ConverterRegistry {

    private final Map<ConvertType, Converters> convertersMap = new EnumMap<>(ConvertType.class);


    @Override
    public void addConverter(ConvertType convertType, Converter converter) {
        Converters converters = convertersMap.computeIfAbsent(convertType, k -> new Converters());
        converters.addConverter(converter);
    }



    /**
     * 类型转换
     * @param convertType 转换规则
     * @param source 转换对象
     * @param typeDescriptor 目标类型
     * @return 转换后的对象
     */
    public Object convert(ConvertType convertType, Object source, TypeDescriptor typeDescriptor) throws ConversionException {
        TypeDescriptor sourceTypeDescriptor = TypeDescriptor.forObject(source);
        Converters converters = convertersMap.get(convertType);
        if (converters == null) {
            throw new ConverterNotFoundException(sourceTypeDescriptor, typeDescriptor);
        }
        Converter converter = converters.getConverter(sourceTypeDescriptor.getType(), typeDescriptor.getType());
        if (converter == null) {
            throw new ConverterNotFoundException(sourceTypeDescriptor, typeDescriptor);
        }
        return converter.convert(source, typeDescriptor);
    }



    private static class Converters {

        private final Map<ConvertiblePair, Converter> converters;

        public Converters() {
            converters = new ConcurrentHashMap<>(2);
        }

        public void addConverter(Converter converter) {
            converters.put(converter.getConvertibleType(), converter);
        }

        public Converter getConverter(Class<?> sourceType, Class<?> targetType) {
            return converters.get(new ConvertiblePair(sourceType, targetType));
        }
    }

    /**
     * Holder for a source-to-target class pair.
     */
    public static final class ConvertiblePair {

        private final Class<?> sourceType;

        private final Class<?> targetType;

        /**
         * Create a new source-to-target pair.
         * @param sourceType the source type
         * @param targetType the target type
         */
        public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
            Assert.notNull(sourceType, "Source type must not be null");
            Assert.notNull(targetType, "Target type must not be null");
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public Class<?> getSourceType() {
            return this.sourceType;
        }

        public Class<?> getTargetType() {
            return this.targetType;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || other.getClass() != ConvertiblePair.class) {
                return false;
            }
            ConvertiblePair otherPair = (ConvertiblePair) other;
            return (this.sourceType == otherPair.sourceType && this.targetType == otherPair.targetType);
        }

        @Override
        public int hashCode() {
            return (this.sourceType.hashCode() * 31 + this.targetType.hashCode());
        }

        @Override
        public String toString() {
            return (this.sourceType.getName() + " -> " + this.targetType.getName());
        }
    }

}
