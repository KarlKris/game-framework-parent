package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.CollectionUtils;
import com.echo.common.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * string to map  key1:value1,key2:value2
 * @author: li-yuanwen
 */
final class StringToMapConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;

    public StringToMapConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        TypeDescriptor mapKeyTypeDescriptor = targetType.getMapKeyTypeDescriptor();
        boolean keyMatch = mapKeyTypeDescriptor == null || this.conversionService.canConvert(sourceType, mapKeyTypeDescriptor);
        if (!keyMatch) {
            return false;
        }
        return targetType.getMapValueTypeDescriptor() == null
                || this.conversionService.canConvert(sourceType, targetType.getMapValueTypeDescriptor());
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Map.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        String string = (String) source;

        String[] fields = StringUtils.delimitedListToStringArray(string, ",");

        TypeDescriptor mapKeyTypeDescriptor = targetType.getMapKeyTypeDescriptor();
        TypeDescriptor mapValueTypeDescriptor = targetType.getMapValueTypeDescriptor();
        Map<Object, Object> map = CollectionUtils.createMap(targetType.getType()
                , mapKeyTypeDescriptor == null ? null : mapKeyTypeDescriptor.getType(), fields.length);
        for (String field : fields) {
            String[] keyValue = StringUtils.delimitedListToStringArray(field, ":");
            Object key = convertKeyValue(keyValue[0], sourceType, mapKeyTypeDescriptor);
            Object value = convertKeyValue(keyValue[1], sourceType, mapValueTypeDescriptor);
            map.put(key, value);
        }
        return map;
    }

    private Object convertKeyValue(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType == null) {
            return source;
        }
        return this.conversionService.convert(source, sourceType, targetType);
    }

}

