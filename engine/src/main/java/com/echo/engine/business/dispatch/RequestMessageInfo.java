package com.echo.engine.business.dispatch;

/**
 * 线程中执行的协议的相关信息
 *
 * @author: li-yuanwen
 */
public class RequestMessageInfo {

    /**
     * 玩家唯一标识
     **/
    private final static ThreadLocal<Long> IDENTITY_THREAD_LOCAL = new ThreadLocal<>();

    public static void setIdentity(long identity) {
        IDENTITY_THREAD_LOCAL.set(identity);
    }

    public static Long getIdentity() {
        return IDENTITY_THREAD_LOCAL.get();
    }

    public static void removeIdentity() {
        IDENTITY_THREAD_LOCAL.remove();
    }

    // ----------------------------------------------------------------------------------------

    /**
     * 请求消息序列号
     **/
    private final static ThreadLocal<Long> MESSAGE_SN_THREAD_LOCAL = new ThreadLocal<>();

    public static void setMessageSn(long messageSn) {
        MESSAGE_SN_THREAD_LOCAL.set(messageSn);
    }

    public static Long getMessageSn() {
        return MESSAGE_SN_THREAD_LOCAL.get();
    }

    public static void removeMessageSn() {
        MESSAGE_SN_THREAD_LOCAL.remove();
    }

}
