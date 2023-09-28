package com.echo.network.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

/**
 * @author li-yuanwen
 * 消息推送中消息体的封装
 */
@Getter
@AllArgsConstructor
public class PushResponse {

    /**
     * 推送目标标识
     **/
    private final Collection<Long> targets;
    /**
     * 推送内容(不允许压缩)
     **/
    private final byte[] content;

}
