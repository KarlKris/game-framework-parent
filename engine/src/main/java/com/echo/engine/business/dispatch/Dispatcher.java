package com.echo.engine.business.dispatch;


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
     * @param message 消息
     * @param session session
     */
    void dispatch(final M message, final S session);


}
