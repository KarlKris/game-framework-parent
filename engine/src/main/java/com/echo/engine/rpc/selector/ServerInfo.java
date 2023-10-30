package com.echo.engine.rpc.selector;

import com.echo.engine.client.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务器信息
 *
 * @author li-yuanwen
 * @date 2021/8/7 22:16
 **/
@Getter
@AllArgsConstructor
public class ServerInfo {

    /**
     * 服务器标识
     **/
    private int id;
    /**
     * 服务器类型
     **/
    private byte type;
    /**
     * 服务器ip地址
     **/
    private Address address;
    /**
     * 在线人数
     **/
    private int onlineNum;

}
