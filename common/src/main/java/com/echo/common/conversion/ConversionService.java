package com.echo.common.conversion;

import cn.hutool.core.convert.BasicType;
import cn.hutool.core.lang.Assert;
import com.echo.common.conversion.converter.Converter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

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


    public Converter getConverter(ConvertType convertType, TypeDescriptor typeDescriptor, TypeDescriptor targetDescriptor) {
        Converters converters = convertersMap.get(convertType);
        if (converters == null) {
            throw new ConverterNotFoundException(typeDescriptor, targetDescriptor);
        }
        Converter converter = converters.getConverter(typeDescriptor.getType(), targetDescriptor.getType());
        if (converter == null) {
            throw new ConverterNotFoundException(typeDescriptor, targetDescriptor);
        }
        return converter;
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
        Converter converter = getConverter(convertType, sourceTypeDescriptor, typeDescriptor);
        return converter.convert(source, typeDescriptor);
    }



    private static class Converters {

        private final List<Converter> converters;

        public Converters() {
            converters = new CopyOnWriteArrayList<>();
        }

        public void addConverter(Converter converter) {
            converters.add(converter);
        }

        public Converter getConverter(Class<?> sourceType, Class<?> targetType) {
            ConvertiblePair matchConvertiblePair = new ConvertiblePair(sourceType, targetType);
            for (Converter converter : converters) {
                ConvertiblePair convertiblePair = converter.getConvertiblePair();
                if (convertiblePair.isConvert(matchConvertiblePair)) {
                    return converter;
                }
            }
            return null;
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


        public boolean isConvert(ConvertiblePair pair) {
            if (this == pair) return true;
            if (!Objects.equals(sourceType, pair.sourceType)) {
                return false;
            }
            Class<?> compareTargetType = pair.targetType;
            if (compareTargetType.isPrimitive()) {
                compareTargetType = BasicType.wrap(compareTargetType);
            }
            return targetType.isAssignableFrom(compareTargetType);
        }

        @Override
        public String toString() {
            return (this.sourceType.getName() + " -> " + this.targetType.getName());
        }
    }

}
