package com.echo.common.convert.support;

import com.echo.common.convert.converter.GenericConverter;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.convert.exception.ConversionFailedException;
import com.echo.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collections;
import java.util.Set;

/**
 * obj 转换 Json
 * OBJECT--> STRING
 */
public class ObjToJsonConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, String.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        try {
            return JsonUtils.toJson(source);
        } catch (JsonProcessingException e) {
            String message = MessageFormatter.format("object[{}]无法转换成Json", source.getClass().getName()).getMessage();
            throw new ConversionFailedException(TypeDescriptor.forObject(source)
                    , sourceType, message, e);
        }
    }
}
