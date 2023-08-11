package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalConverter;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.ClassUtils;

/**
 * 基于枚举的转换器
 * @author: li-yuanwen
 */
abstract class AbstractConditionalEnumConverter implements ConditionalConverter {

    private final ConversionService conversionService;


    protected AbstractConditionalEnumConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        for (Class<?> interfaceType : ClassUtils.getAllInterfacesForClassAsSet(sourceType.getType())) {
            if (this.conversionService.canConvert(TypeDescriptor.valueOf(interfaceType), targetType)) {
                return false;
            }
        }
        return true;
    }

}
