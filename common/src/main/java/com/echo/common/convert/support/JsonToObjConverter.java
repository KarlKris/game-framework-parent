package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.convert.exception.ConversionFailedException;
import com.echo.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collection;
import java.util.Map;

/**
 * Json 转换 obj
 * STRING--> OBJECT
 */
public class JsonToObjConverter implements ConverterFactory<String, Object> {


    @Override
    public <T> Converter<String, T> getConverter(Class<T> targetType) {
        return new JsonToObject<>(targetType);
    }


    private static class JsonToObject<T> implements Converter<String, T> {

        private final TypeDescriptor typeDescriptor;

        public JsonToObject(Class<T> targetType) {
            this.typeDescriptor = TypeDescriptor.valueOf(targetType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            try {
                JavaType javaType = null;
                if (String.class.isAssignableFrom(typeDescriptor.getType())) {
                    return (T) source;
                }
                if (typeDescriptor.isCollection()) {
                    javaType = JsonUtils.TYPE_FACTORY.constructCollectionType( (Class<? extends Collection<?>>) typeDescriptor.getType()
                            , typeDescriptor.getElementTypeDescriptor().getType());
                } else if(typeDescriptor.isArray()) {
                    TypeDescriptor elementType = typeDescriptor.getElementTypeDescriptor();
                    if (elementType.isPrimitive()) {
                        javaType = JsonUtils.TYPE_FACTORY.constructType(typeDescriptor.getObjectType());
                    } else {
                        javaType = JsonUtils.TYPE_FACTORY.constructArrayType(elementType.getType());
                    }
                } else if(typeDescriptor.isMap()){
                    javaType = JsonUtils.TYPE_FACTORY.constructMapType((Class<? extends Map<?, ?>>) typeDescriptor.getType()
                            , typeDescriptor.getMapKeyTypeDescriptor().getType()
                            , typeDescriptor.getMapValueTypeDescriptor().getType());
                } else {
                    javaType = JsonUtils.TYPE_FACTORY.constructType(typeDescriptor.getType());
                }
                return (T) JsonUtils.toObj(source, javaType);
            } catch (JsonProcessingException e) {
                String message = MessageFormatter.format("字符串[{}]无法转换成指定类型[{}]"
                        , source.getClass().getName(), typeDescriptor.getType()).getMessage();
                throw new ConversionFailedException(TypeDescriptor.forObject(source), typeDescriptor, message, e);
            }
        }
    }


}
