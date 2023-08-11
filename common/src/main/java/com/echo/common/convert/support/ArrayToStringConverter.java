package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * array to string
 * @author: li-yuanwen
 */
final class ArrayToStringConverter implements ConditionalGenericConverter {

    private final CollectionToStringConverter helperConverter;


    public ArrayToStringConverter(ConversionService conversionService) {
        this.helperConverter = new CollectionToStringConverter(conversionService);
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object[].class, String.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return this.helperConverter.matches(sourceType, targetType);
    }

    @Override
    public Object convert( Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return this.helperConverter.convert(Arrays.asList(ObjectUtils.toObjectArray(source)), sourceType, targetType);
    }

}
