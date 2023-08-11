package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.GenericConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * array to array
 * @author: li-yuanwen
 */
final class ArrayToArrayConverter implements ConditionalGenericConverter {

    private final CollectionToArrayConverter helperConverter;

    private final ConversionService conversionService;


    public ArrayToArrayConverter(ConversionService conversionService) {
        this.helperConverter = new CollectionToArrayConverter(conversionService);
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object[].class, Object[].class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return this.helperConverter.matches(sourceType, targetType);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (this.conversionService instanceof GenericConversionService) {
            TypeDescriptor targetElement = targetType.getElementTypeDescriptor();
            if (targetElement != null &&
                    ((GenericConversionService) this.conversionService).canBypassConvert(
                            sourceType.getElementTypeDescriptor(), targetElement)) {
                return source;
            }
        }
        List<Object> sourceList = Arrays.asList(ObjectUtils.toObjectArray(source));
        return this.helperConverter.convert(sourceList, sourceType, targetType);
    }

}

