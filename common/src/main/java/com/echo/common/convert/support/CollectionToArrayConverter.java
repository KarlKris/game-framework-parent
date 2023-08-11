package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Collection to Array
 * @author: li-yuanwen
 */
final class CollectionToArrayConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public CollectionToArrayConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, Object[].class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConditionalGenericConverter.canConvertElements(sourceType.getElementTypeDescriptor(),
                targetType.getElementTypeDescriptor(), this.conversionService);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Collection<?> sourceCollection = (Collection<?>) source;
        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();
        if (targetElementType == null) {
            throw new IllegalStateException("No target element type");
        }
        Object array = Array.newInstance(targetElementType.getType(), sourceCollection.size());
        int i = 0;
        for (Object sourceElement : sourceCollection) {
            Object targetElement = this.conversionService.convert(sourceElement,
                    sourceType.elementTypeDescriptor(sourceElement), targetElementType);
            Array.set(array, i++, targetElement);
        }
        return array;
    }

}
