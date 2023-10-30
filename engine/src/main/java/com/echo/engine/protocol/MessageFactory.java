package com.echo.engine.protocol;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ZipUtil;
import com.echo.common.util.StringUtils;
import com.echo.engine.config.NettyServerSettings;
import com.echo.network.message.*;
import com.echo.network.session.PlayerSession;

/**
 * 消息工厂
 * 推送,应答前只能通过工厂构建消息,不允许私自调用对应消息的构造方法或静态创建方法
 *
 * @author: li-yuanwen
 */
public class MessageFactory {

    /**
     * 配置
     **/
    private final NettyServerSettings settings;

    public MessageFactory(NettyServerSettings settings) {
        this.settings = settings;
    }


    /**
     * 构建内部消息
     *
     * @param sn            消息序号
     * @param type          消息类型
     * @param protocol      协议
     * @param body          消息体
     * @param ip            ip
     * @return 内部消息
     */
    public InnerMessage toInnerMessage(long sn, byte type, SocketProtocol protocol
            , byte[] body, long identity, String ip) {
        boolean zip = false;
        if (ArrayUtil.isNotEmpty(body) && body.length > settings.getBodyZipLength()) {
            body = ZipUtil.gzip(body);
            zip = true;
        }
        byte[] ipBytes = StringUtils.hasLength(ip) ? ip.getBytes() : null;
        InnerMessageHeader header = InnerMessageHeader.of(type, protocol, zip, sn, identity, ipBytes);
        return InnerMessage.of(header, body);
    }


    /**
     * 构建外部消息
     *
     * @param sn            消息序号
     * @param type          消息类型
     * @param protocol      协议
     * @param serializeType 序列化类型
     * @param body          消息体
     * @return 外部消息
     */
    public OuterMessage toOuterMessage(long sn, byte type, SocketProtocol protocol
            , byte[] body) {
        boolean zip = false;
        if (ArrayUtil.isNotEmpty(body) && body.length > settings.getBodyZipLength()) {
            body = ZipUtil.gzip(body);
            zip = true;
        }
        OuterMessageHeader header = OuterMessageHeader.of(sn, type, protocol, zip);
        return OuterMessage.of(header, body);
    }

    public InnerMessage convertToRequestInnerMessage(PlayerSession playerSession, long sn, OuterMessage outerMessage) {
        return toInnerMessage(sn
                , outerMessage.getMessageType()
                , outerMessage.getProtocol()
                , outerMessage.getBody()
                , playerSession.getIdentity()
                , playerSession.getIp());
    }


    public OuterMessage convertToResponseOuterMessage(long sn, InnerMessage innerMessage) {
        return toOuterMessage(sn
                , innerMessage.getMessageType()
                , innerMessage.getProtocol()
                , innerMessage.getBody());

    }

}
