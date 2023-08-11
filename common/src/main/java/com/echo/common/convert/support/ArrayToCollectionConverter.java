package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.converter.GenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Array to Collection
 * @author: li-yuanwen
 */
final class ArrayToCollectionConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public ArrayToCollectionConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(Object[].class, Collection.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConditionalGenericConverter.canConvertElements(
                sourceType.getElementTypeDescriptor(), targetType.getElementTypeDescriptor(), this.conversionService);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }

        int length = Array.getLength(source);
        TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
        Collection<Object> target = CollectionUtils.create(targetType.getType(),
                (elementDesc != null ? elementDesc.getType() : null), length);

        if (elementDesc == null) {
            for (int i = 0; i < length; i++) {
                Object sourceElement = Array.get(source, i);
                target.add(sourceElement);
            }
        }
        else {
            for (int i = 0; i < length; i++) {
                Object sourceElement = Array.get(source, i);
                Object targetElement = this.conversionService.convert(sourceElement,
                        sourceType.elementTypeDescriptor(sourceElement), elementDesc);
                target.add(targetElement);
            }
        }
        return target;
    }

}

