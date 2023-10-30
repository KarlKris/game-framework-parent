package com.echo.engine.client;


import com.echo.engine.rpc.core.Invocation;
import com.echo.network.message.IMessage;

/**
 * Netty Client 接口
 *
 * @author li-yuanwen
 * @date 2021/8/1 17:45
 **/
public interface NettyClient {

    /**
     * 链接地址
     *
     * @return address
     */
    Address getAddress();

    /**
     * 发送消息
     *
     * @param message    消息
     * @param invocation invocation
     * @throws InterruptedException 连接不上对方时抛出
     */
    void send(IMessage message, Invocation invocation) throws InterruptedException;


    /**
     * 获取远程对象的代理
     *
     * @param clz 类对象
     * @param <T> 类
     * @return /
     */
    <T> T getSendProxy(Class<T> clz);

}
