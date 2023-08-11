package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;

/**
 * string to array
 * @author: li-yuanwen
 */
final class StringToArrayConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public StringToArrayConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Object[].class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConditionalGenericConverter.canConvertElements(sourceType, targetType.getElementTypeDescriptor(),
                this.conversionService);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        String string = (String) source;
        String[] fields = StringUtils.delimitedListToStringArray(string, ",");
        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();
        if (targetElementType == null) {
            throw new IllegalArgumentException("No target element type");
        }
        Object target = Array.newInstance(targetElementType.getType(), fields.length);
        for (int i = 0; i < fields.length; i++) {
            String sourceElement = fields[i];
            Object targetElement = this.conversionService.convert(sourceElement.trim(), sourceType, targetElementType);
            Array.set(target, i, targetElement);
        }
        return target;
    }
}
