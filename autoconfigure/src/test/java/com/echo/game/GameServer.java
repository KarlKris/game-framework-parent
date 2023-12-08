package com.echo.game;

import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.client.Address;
import com.echo.engine.client.NettyClient;
import com.echo.engine.rpc.core.GenericRemoteLocalServerSeekOperation;
import com.echo.engine.rpc.core.Invocation;
import com.echo.engine.rpc.core.RemoteServerSeekOperation;
import com.echo.engine.rpc.selector.ServerInfo;
import com.echo.ioc.context.AnnotationGenericApplicationContext;
import com.echo.network.message.InnerMessage;

import java.net.SocketException;

/**
 * 游戏服
 *
 * @author: li-yuanwen
 */
public class GameServer {

    public static void main(String[] args) throws InterruptedException, SocketException {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        NettyServerBootstrap bootstrap = context.getBean(NettyServerBootstrap.class);
        bootstrap.startServer();

        GenericRemoteLocalServerSeekOperation seekOperation = (GenericRemoteLocalServerSeekOperation) context
                .getBean(RemoteServerSeekOperation.class);
        seekOperation.register(new ServerInfo(1, (byte) 1, new Address("127.0.0.1", 9630), 0));

        NettyClient nettyClient = seekOperation.seekApplication((byte) 1, 1);
        InnerMessage message = InnerMessage.HEART_BEAT_REQ;
        nettyClient.send(message, new Invocation(message.getSn(), 1L, 1, true) {
            @Override
            public void complete(InnerMessage message) {
                System.out.println(message);
            }
        });
    }


}
