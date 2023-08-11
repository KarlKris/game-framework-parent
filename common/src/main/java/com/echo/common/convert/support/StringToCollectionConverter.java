package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalGenericConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.CollectionUtils;
import com.echo.common.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * string to collection
 * @author: li-yuanwen
 */
final class StringToCollectionConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public StringToCollectionConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return (targetType.getElementTypeDescriptor() == null ||
                this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor()));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        String string = (String) source;

        String[] fields = StringUtils.delimitedListToStringArray(string, ",");
        TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
        Collection<Object> target = CollectionUtils.create(targetType.getType(),
                (elementDesc != null ? elementDesc.getType() : null), fields.length);

        if (elementDesc == null) {
            for (String field : fields) {
                target.add(field.trim());
            }
        }
        else {
            for (String field : fields) {
                Object targetElement = this.conversionService.convert(field.trim(), sourceType, elementDesc);
                target.add(targetElement);
            }
        }
        return target;
    }

}

