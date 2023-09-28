package com.echo.network.protocol;

import com.echo.common.util.ClassUtils;
import com.echo.common.util.ReflectionUtils;
import lombok.Getter;

/**
 * @author li-yuanwen
 * @date 2021/7/31 14:11
 * 方法调用上下文
 **/
@Getter
public class ProtocolMethodInvocation {

    /**
     * 目标对象
     **/
    private final Object target;
    /**
     * 方法上下文
     **/
    private final ProtocolMethod protocolMethod;
    /**
     * 是否需要身份标识
     **/
    private final boolean identity;

    ProtocolMethodInvocation(Object target, ProtocolMethod protocolMethod) {
        this.target = target;
        this.identity = protocolMethod.identity();
        this.protocolMethod = protocolMethod;

    }

    /**
     * 判断命令逻辑处理的返回结果是否是Void.class
     *
     * @return false Void.class
     */
    public boolean hasResponseClass() {
        Class<?> returnType = protocolMethod.getMethod().getReturnType();
        return ClassUtils.isAssignable(Void.TYPE, returnType);
    }

    /**
     * 协议是否是同步协议
     *
     * @return true 同步协议
     */
    public boolean isSyncMethod() {
        return protocolMethod.isSyncMethod();
    }


    public Object invokeMethod(Object[] args) {
        return ReflectionUtils.invokeMethod(protocolMethod.getMethod(), target, args);
    }

}
