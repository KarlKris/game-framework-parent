package com.echo.common.convert.converter;

import com.echo.common.convert.core.TypeDescriptor;

import java.util.Set;

/**
 * N:N的转换
 */
public interface GenericConverter {

    /**
     * 返回此转换器可以在其之间转换的源和目标类型。
     * 每个条目都是可转换的源到目标类型对。
     * 对于条件转换器{@link }，此方法可能返回 null，以指示应考虑所有源到目标对。
     * @return null or
     */
    Set<ConvertiblePair> getConvertibleTypes();


    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);

    /**
     * 表示一对，包含sourceType和targetType
     */
    final class ConvertiblePair {

        private final Class<?> sourceType;

        private final Class<?> targetType;

        /**
         * Create a new source-to-target pair.
         * @param sourceType the source type
         * @param targetType the target type
         */
        public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
            if (sourceType == null) {
                throw new IllegalArgumentException("Source type must not be null");
            }
            if (targetType == null) {
                throw new IllegalArgumentException("Target type must not be null");
            }
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
        public boolean equals(Object other) {
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
