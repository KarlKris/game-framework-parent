package com.echo.engine.business;


import com.echo.network.message.IMessage;
import com.echo.network.session.ISession;

/**
 * @author li-yuanwen
 * @date 2021/7/31 15:40
 * 消息分发器接口
 **/
public interface Dispatcher<M extends IMessage, S extends ISession> {

    /**
     * 消息分发
     *
     * @param session session
     * @param message 消息
     */
    void dispatch(final S session, final M message);


}
