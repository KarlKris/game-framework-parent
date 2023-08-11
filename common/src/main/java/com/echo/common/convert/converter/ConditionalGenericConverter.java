package com.echo.common.convert.converter;

import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.ClassUtils;

/**
 * GenericConverter和ConditionalConverter 接口组合
 */
public interface ConditionalGenericConverter extends GenericConverter, ConditionalConverter {

    static boolean canConvertElements(TypeDescriptor sourceElementType,
                                             TypeDescriptor targetElementType, ConversionService conversionService) {

        if (targetElementType == null) {
            // yes
            return true;
        }
        if (sourceElementType == null) {
            // maybe
            return true;
        }
        if (conversionService.canConvert(sourceElementType, targetElementType)) {
            // yes
            return true;
        }
        if (ClassUtils.isAssignable(sourceElementType.getType(), targetElementType.getType())) {
            // maybe
            return true;
        }
        // no
        return false;
    }

}
