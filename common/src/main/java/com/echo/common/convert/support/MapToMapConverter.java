package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.CollectionUtils;

import java.util.*;

/**
 * Map to Map
 * @author: li-yuanwen
 */
final class MapToMapConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public MapToMapConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Map.class, Map.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return canConvertKey(sourceType, targetType) && canConvertValue(sourceType, targetType);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<Object, Object> sourceMap = (Map<Object, Object>) source;

        // Shortcut if possible...
        boolean copyRequired = !targetType.getType().isInstance(source);
        if (!copyRequired && sourceMap.isEmpty()) {
            return sourceMap;
        }
        TypeDescriptor keyDesc = targetType.getMapKeyTypeDescriptor();
        TypeDescriptor valueDesc = targetType.getMapValueTypeDescriptor();

        List<MapEntry> targetEntries = new ArrayList<>(sourceMap.size());
        for (Map.Entry<Object, Object> entry : sourceMap.entrySet()) {
            Object sourceKey = entry.getKey();
            Object sourceValue = entry.getValue();
            Object targetKey = convertKey(sourceKey, sourceType, keyDesc);
            Object targetValue = convertValue(sourceValue, sourceType, valueDesc);
            targetEntries.add(new MapEntry(targetKey, targetValue));
            if (sourceKey != targetKey || sourceValue != targetValue) {
                copyRequired = true;
            }
        }
        if (!copyRequired) {
            return sourceMap;
        }

        Map<Object, Object> targetMap = CollectionUtils.createMap(targetType.getType(),
                (keyDesc != null ? keyDesc.getType() : null), sourceMap.size());

        for (MapEntry entry : targetEntries) {
            entry.addToMap(targetMap);
        }
        return targetMap;
    }


    // internal helpers

    private boolean canConvertKey(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConditionalGenericConverter.canConvertElements(sourceType.getMapKeyTypeDescriptor(),
                targetType.getMapKeyTypeDescriptor(), this.conversionService);
    }

    private boolean canConvertValue(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConditionalGenericConverter.canConvertElements(sourceType.getMapValueTypeDescriptor(),
                targetType.getMapValueTypeDescriptor(), this.conversionService);
    }

    private Object convertKey(Object sourceKey, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType == null) {
            return sourceKey;
        }
        return this.conversionService.convert(sourceKey, sourceType.getMapKeyTypeDescriptor(sourceKey), targetType);
    }

    private Object convertValue(Object sourceValue, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType == null) {
            return sourceValue;
        }
        return this.conversionService.convert(sourceValue, sourceType.getMapValueTypeDescriptor(sourceValue), targetType);
    }


    private static class MapEntry {

        private final Object key;

        private final Object value;

        public MapEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public void addToMap(Map<Object, Object> map) {
            map.put(this.key, this.value);
        }
    }

}
