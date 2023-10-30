package com.echo.autoconfigure.framework.modules;

/**
 * 账号相关模块
 */
public interface AccountModule {

    /**
     * 模块号
     **/
    short MODULE = 101;

    /**
     * 创建账号
     **/
    byte CREATE_ACCOUNT = 1;
    /**
     * 登录账号
     **/
    byte LOGON_ACCOUNT = 2;

}
