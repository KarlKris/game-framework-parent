package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;

/**
 * collection to string
 * @author: li-yuanwen
 */
final class CollectionToStringConverter implements ConditionalGenericConverter {

    private static final String DELIMITER = ",";

    private final ConversionService conversionService;


    public CollectionToStringConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, String.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConditionalGenericConverter.canConvertElements(
                sourceType.getElementTypeDescriptor(), targetType, this.conversionService);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Collection<?> sourceCollection = (Collection<?>) source;
        if (sourceCollection.isEmpty()) {
            return "";
        }
        StringJoiner sj = new StringJoiner(DELIMITER);
        for (Object sourceElement : sourceCollection) {
            Object targetElement = this.conversionService.convert(
                    sourceElement, sourceType.elementTypeDescriptor(sourceElement), targetType);
            sj.add(String.valueOf(targetElement));
        }
        return sj.toString();
    }

}
