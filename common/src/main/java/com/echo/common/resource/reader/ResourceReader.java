package com.echo.common.resource.reader;

import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.convert.exception.ConverterNotFoundException;
import com.echo.common.util.ReflectionUtils;
import com.echo.common.util.TypeDescriptorUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 文件资源读取器
 * @author li-yuanwen
 * @date 2022/3/17
 */
public interface ResourceReader {

    /**
     * 读取的文件后缀名
     * @return 文件后缀
     */
    String getFileSuffix();

    /**
     * 资源读取
     * @param in 资源Input
     * @param clz 目标类型
     * @param <E> 实际类型
     * @return 资源集
     */
    <E> List<E> read(InputStream in, Class<E> clz);


    /** 抽象属性解析器 **/
    abstract class AbstractFieldResolver {

        /** 属性 **/
        private final Field field;
        /** TypeDescriptor **/
        private final TypeDescriptor descriptor;
        /** 转换器 **/
        private final ConversionService conversionService;

        public AbstractFieldResolver(Field field, ConversionService conversionService) {
            this.field = field;
            this.descriptor = TypeDescriptorUtils.newInstance(field);
            ReflectionUtils.makeAccessible(field);
            this.conversionService = conversionService;
        }

        /** 获取属性名称 **/
        public String getFieldName() {
            return field.getName();
        }

        /** 属性实例注入 **/
        public void inject(Object instance, String content) {
            try {
                Object value = conversionService.convert(content, TypeDescriptorUtils.STRING_DESCRIPTOR, descriptor);
                field.set(instance, value);
            } catch (ConverterNotFoundException e) {
                FormattingTuple message = MessageFormatter.format("静态资源[{}]属性[{}]的转换器不存在",
                        instance.getClass().getSimpleName(), field.getName());
                throw new IllegalStateException(message.getMessage(), e);
            } catch (IllegalAccessException e) {
                FormattingTuple message = MessageFormatter.format("静态资源[{}]属性[{}]注入失败", instance.getClass(), field);
                throw new IllegalStateException(message.getMessage(), e);
            }

        }

    }

}
