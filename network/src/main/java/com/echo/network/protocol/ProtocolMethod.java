package com.echo.network.protocol;

import com.echo.network.message.SocketProtocol;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author li-yuanwen
 * 方法上下文
 */
@Getter
public class ProtocolMethod {

    /**
     * 命令
     **/
    private final SocketProtocol protocol;
    /**
     * 具体方法
     **/
    private final Method method;
    /**
     * 方法参数
     **/
    private final MethodParameter[] params;
    /**
     * 立即返回结果(用于同步方法)
     **/
    private final boolean syncMethod;
    /**
     * 返回对象类型
     **/
    private Class<?> returnClz;

    public ProtocolMethod(SocketProtocol protocol, Method method, MethodParameter[] params, boolean syncMethod) {
        this.protocol = protocol;
        this.method = method;
        this.params = params;
        this.syncMethod = syncMethod;
    }

    public ProtocolMethod(SocketProtocol protocol, Method method, MethodParameter[] params, boolean syncMethod, Class<?> returnClz) {
        this.protocol = protocol;
        this.method = method;
        this.params = params;
        this.syncMethod = syncMethod;
        this.returnClz = returnClz;
    }

    public void setReturnClz(Class<?> clz) {
        this.returnClz = clz;
    }

    public Class<?> getReturnClz() {
        return returnClz;
    }

    /**
     * 方法是否需要身份标识
     **/
    boolean identity() {
        for (MethodParameter parameter : params) {
            if (parameter instanceof IdentityMethodParameter) {
                return true;
            }
        }
        return false;
    }
}
