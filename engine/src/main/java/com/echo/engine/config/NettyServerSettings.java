package com.echo.engine.config;

import com.echo.network.serialize.SerializeType;
import lombok.Builder;
import lombok.Getter;

/**
 * netty server端 相关配置信息
 *
 * @author: li-yuanwen
 */
@Getter
@Builder
public class NettyServerSettings {

    /**
     * 服务器标识
     */
    private int serverId;
    /**
     * 服务器类型
     */
    private byte type;
    /**
     * 端口号
     **/
    private int port;
    /**
     * worker线程数
     **/
    private int threadNum;
    /**
     * 业务线程数
     **/
    private int businessThreadNum;
    /**
     * TCP参数SO_BACKLOG
     **/
    private int backLog = 1024;
    /**
     * ssl相关配置
     **/
    private OpenSslSettings sslSettings;

    /**
     * 心跳检测 readerIdleTime
     **/
    private int serverReaderIdleTime = 30;
    /**
     * 心跳检测 writerIdleTime
     **/
    private int clientWriterIdleTime = 25;
    /**
     * 心跳检测 allIdleTime
     **/
    private int allIdleTime;

    /**
     * 消息体进行压缩的长度值 默认10K
     **/
    private int bodyZipLength = 10 * 1024;

    /**
     * 消息序列化方式(0 Protobuf,1 Json)
     **/
    private byte serializeType = SerializeType.PROTOBUF.getType();


    public int getIOThreadNum() {
        if (threadNum == 0) {
            return Runtime.getRuntime().availableProcessors() << 1;
        }
        return threadNum;
    }

    public int getFinalBusinessThreadNum() {
        if (businessThreadNum == 0) {
            return Runtime.getRuntime().availableProcessors() << 1;
        }
        return businessThreadNum;
    }

    // ------------ ssl ---------------

    @Getter
    @Builder
    public static final class OpenSslSettings {

        /**
         * openssl 版本协议
         **/
        private String protocol;
        /**
         * ca证书地址
         **/
        private String caPath;

        /**
         * 服务端crt path
         **/
        private String serverCrtPath;
        /**
         * 服务端ssl 秘钥pkcs#8编码
         **/
        private String serverPkcs8keyPath;

        /**
         * 客户端crt path
         **/
        private String clientCrtPath;
        /**
         * 客户端ssl 秘钥pkcs#8编码
         **/
        private String clientPkcs8keyPath;
    }


}
