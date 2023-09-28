package com.echo.engine.config;

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
     * 端口号
     **/
    private int port;
    /**
     * worker线程数
     **/
    private int threadNum;
    /**
     * worker线程数
     **/
    private int handeThreadNum;
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


    public int getIOThreadNum() {
        if (threadNum == 0) {
            return Runtime.getRuntime().availableProcessors() << 1;
        }
        return threadNum;
    }

    public int getHandleThreadNum() {
        if (handeThreadNum == 0) {
            return Runtime.getRuntime().availableProcessors() << 1;
        }
        return handeThreadNum;
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
