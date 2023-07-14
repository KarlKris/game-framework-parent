package com.echo.common.resource.resolver;

import com.echo.common.util.ReflectionUtils;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Field;

/**
 * 属性解析器
 * @author li-yuanwen
 * @date 2022/3/22
 */
public class FieldResolver implements Resolver {

    /** 属性 **/
    private final Field field;

    FieldResolver(Field field) {
        ReflectionUtils.makeAccessible(field);
        this.field = field;
    }

    @Override
    public final Object resolve(Object obj) {
        Object result = null;
        try {
            result = field.get(obj);
        } catch (Exception e) {
            String message = MessageFormatter.arrayFormat("解析对象:{} 属性:{} 出现未知异常"
                    , new Object[]{obj.getClass().getName(), field.getName(), e}).getMessage();
            throw new RuntimeException(message);
        }
        return result;
    }
}
