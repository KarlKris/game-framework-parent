package com.echo.network.session;


import com.echo.network.message.IMessage;
import io.netty.channel.Channel;

/**
 * 网络层连接对象接口
 *
 * @author li-yuanwen
 * @date 2021/12/8
 */
public interface ISession {


    /**
     * 获取连接对象唯一标识
     *
     * @return 唯一标识
     */
    long getSessionId();

    /**
     * 获取连接对象的ip地址
     *
     * @return ip地址
     */
    String getIp();

    /**
     * 获取连接对象channel
     *
     * @return channel
     */
    Channel getChannel();

    /**
     * 传输消息
     *
     * @param message 消息
     */
    void writeAndFlush(IMessage message);

    /**
     * 关闭连接
     */
    void close();

    /**
     * 绑定身份标识
     *
     * @param identity 身份标识
     * @return PlayerSession
     */
    PlayerSession bindIdentity(long identity);

}
