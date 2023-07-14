package com.echo.common.conversion.converter;

import com.echo.common.conversion.ConversionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.echo.common.conversion.ConversionFailedException;
import com.echo.common.conversion.ConversionService;
import com.echo.common.conversion.TypeDescriptor;
import com.echo.common.util.JsonUtils;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collection;
import java.util.Map;

/**
 * Json 转换 obj
 * STRING--> OBJECT
 */
public class JsonToObjConverter implements Converter {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    static ConversionService.ConvertiblePair CONVERTIBLE_PAIR = new ConversionService.ConvertiblePair(String.class, Object.class);

    @Override
    public ConversionService.ConvertiblePair getConvertiblePair() {
        return CONVERTIBLE_PAIR;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convert(Object source, TypeDescriptor typeDescriptor) throws ConversionException {
        if (!(source instanceof String)) {
            throw new ConversionFailedException(TypeDescriptor.forObject(source)
                    , typeDescriptor, "source obj not string type", null);
        }
        try {
            String content = (String) source;
            JavaType javaType = null;
            if (String.class.isAssignableFrom(typeDescriptor.getType())) {
                return source;
            }
            if (typeDescriptor.isCollection()) {
                javaType = TYPE_FACTORY.constructCollectionType( (Class<? extends Collection<?>>) typeDescriptor.getType()
                        , typeDescriptor.getElementTypeDescriptor().getType());
            } else if(typeDescriptor.isArray()) {
                TypeDescriptor elementType = typeDescriptor.getElementTypeDescriptor();
                if (elementType.isPrimitive()) {
                    javaType = TYPE_FACTORY.constructType(typeDescriptor.getObjectType());
                } else {
                    javaType = TYPE_FACTORY.constructArrayType(elementType.getType());
                }
            } else if(typeDescriptor.isMap()){
                javaType = TYPE_FACTORY.constructMapType((Class<? extends Map<?, ?>>) typeDescriptor.getType()
                        , typeDescriptor.getMapKeyTypeDescriptor().getType()
                        , typeDescriptor.getMapValueTypeDescriptor().getType());
            } else {
                javaType = TYPE_FACTORY.constructType(typeDescriptor.getType());
            }
            return JsonUtils.toObj(content, javaType);
        } catch (JsonProcessingException e) {
            String message = MessageFormatter.format("字符串[{}]无法转换成指定类型[{}]"
                    , source.getClass().getName(), typeDescriptor.getType()).getMessage();
            throw new ConversionFailedException(TypeDescriptor.forObject(source), typeDescriptor, message, e);
        }
    }
}
