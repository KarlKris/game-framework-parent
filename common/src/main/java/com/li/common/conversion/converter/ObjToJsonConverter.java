package com.li.common.conversion.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.li.common.conversion.ConversionException;
import com.li.common.conversion.ConversionFailedException;
import com.li.common.conversion.ConversionService;
import com.li.common.conversion.TypeDescriptor;
import com.li.common.util.JsonUtils;
import org.slf4j.helpers.MessageFormatter;

/**
 * obj 转换 Json
 * OBJECT--> STRING
 */
public class ObjToJsonConverter implements Converter {

    static ConversionService.ConvertiblePair CONVERTIBLE_PAIR = new ConversionService.ConvertiblePair(Object.class, String.class);

    @Override
    public ConversionService.ConvertiblePair getConvertibleType() {
        return CONVERTIBLE_PAIR;
    }

    @Override
    public Object convert(Object source, TypeDescriptor typeDescriptor) throws ConversionException {
        if (!String.class.isAssignableFrom(typeDescriptor.getType())) {
            throw new ConversionFailedException(TypeDescriptor.forObject(source)
                    , typeDescriptor, "target type not string type", null);
        }
        try {
            return JsonUtils.toJson(source);
        } catch (JsonProcessingException e) {
            String message = MessageFormatter.format("object[{}]无法转换成Json[{}]"
                    , source.getClass().getName(), typeDescriptor.getType()).getMessage();
            throw new ConversionFailedException(TypeDescriptor.forObject(source), typeDescriptor, message, e);
        }
    }
}
