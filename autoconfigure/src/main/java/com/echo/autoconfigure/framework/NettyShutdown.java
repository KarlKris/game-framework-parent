package com.echo.autoconfigure.framework;

import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.ioc.context.ApplicationCloseListener;

/**
 * 关闭netty相关线程池
 *
 * @author: li-yuanwen
 */
public class NettyShutdown implements ApplicationCloseListener {

    private final NettyServerBootstrap bootstrap;

    public NettyShutdown(NettyServerBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void applicationClose() {
        bootstrap.shutdown();
    }
}
