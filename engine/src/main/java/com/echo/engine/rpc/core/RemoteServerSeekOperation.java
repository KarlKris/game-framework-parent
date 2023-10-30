package com.echo.engine.rpc.core;

import com.echo.engine.client.NettyClient;
import com.echo.engine.rpc.selector.SelectType;

/**
 * 远程服务器查找接口
 *
 * @author li-yuanwen
 */
public interface RemoteServerSeekOperation {


    /**
     * 查询指定服务器类型并根据身份标识筛选出一个特定的服务器地址
     *
     * @param type     服务器类型
     * @param identity 请求的client的session#identity
     * @return ip地址
     */
    NettyClient seekApplication(byte type, long identity);

    /**
     * 查询指定服务器类型并根据服标识筛选出一个特定的服务器地址
     *
     * @param type     服务器类型
     * @param serverId 服标识
     * @return /
     */
    NettyClient seekApplication(byte type, int serverId);


    /**
     * 查询指定服务器类型并根据选择类型筛选出一个特定的服务器地址
     *
     * @param type       服务器类型
     * @param selectType 选择类型
     * @return /
     */
    NettyClient seekApplication(byte type, SelectType selectType);

    /**
     * 查询指定服务器类型并根据选择类型筛选出一个特定的服务器地址
     *
     * @param type       服务器类型
     * @param selectType 选择类型
     * @return /
     */
    NettyClient seekApplication(byte type, SelectType selectType, Object selectParam);


}
