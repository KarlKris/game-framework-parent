package com.echo.common.resource.resolver;

import com.echo.common.util.ReflectionUtils;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Method;

/**
 * 抽象方法解析器--无方法参数
 * @author li-yuanwen
 * @date 2022/3/22
 */
public class MethodResolver implements Resolver {

    /** 目标方法 **/
    private final Method method;

    MethodResolver(Method method) {
        ReflectionUtils.makeAccessible(method);
        this.method = method;
    }

    @Override
    public final Object resolve(Object obj) {
        Object result = null;
        try {
            result = method.invoke(obj);
        } catch (Exception e) {
            String message = MessageFormatter.arrayFormat("解析对象:{} 方法:{} 出现未知异常"
                    , new Object[]{obj.getClass().getName(), method.getName(), e}).getMessage();
            throw new RuntimeException(message);
        }
        return result;
    }
}
