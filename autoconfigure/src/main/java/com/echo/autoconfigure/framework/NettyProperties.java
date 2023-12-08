package com.echo.autoconfigure.framework;

import com.echo.ioc.anno.ConfigurationProperties;
import com.echo.network.serialize.SerializeType;

/**
 * Netty服务相关配置
 *
 * @author: li-yuanwen
 */
@ConfigurationProperties(prefix = "netty.server")
public class NettyProperties {

    /**
     * 服务器标识
     */
    private int id;
    /**
     * 服务器类型
     */
    private byte type;
    /**
     * 端口
     **/
    private int port;
    /**
     * workers线程数
     **/
    private int workerThreadNum = Runtime.getRuntime().availableProcessors() + 1;
    /**
     * 业务线程数
     **/
    private int businessThreadNum = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 消息序列化方式
     **/
    private byte serializeType = SerializeType.PROTOBUF.getType();

    /**
     * 协议扫描包
     **/
    private String protocolPackage;


    /**
     * ssl
     **/
    private SslConfig ssl;


    // --------------------------------------------------------------


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkerThreadNum() {
        return workerThreadNum;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }

    public int getBusinessThreadNum() {
        return businessThreadNum;
    }

    public void setBusinessThreadNum(int businessThreadNum) {
        this.businessThreadNum = businessThreadNum;
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    public SslConfig getSsl() {
        return ssl;
    }

    public void setSsl(SslConfig ssl) {
        this.ssl = ssl;
    }

    public String getProtocolPackage() {
        return protocolPackage;
    }

    public void setProtocolPackage(String protocolPackage) {
        this.protocolPackage = protocolPackage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    // ----------- ssl ----------------
    public static final class SslConfig {

        /**
         * sll协议
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

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getCaPath() {
            return caPath;
        }

        public void setCaPath(String caPath) {
            this.caPath = caPath;
        }

        public String getServerCrtPath() {
            return serverCrtPath;
        }

        public void setServerCrtPath(String serverCrtPath) {
            this.serverCrtPath = serverCrtPath;
        }

        public String getServerPkcs8keyPath() {
            return serverPkcs8keyPath;
        }

        public void setServerPkcs8keyPath(String serverPkcs8keyPath) {
            this.serverPkcs8keyPath = serverPkcs8keyPath;
        }

        public String getClientCrtPath() {
            return clientCrtPath;
        }

        public void setClientCrtPath(String clientCrtPath) {
            this.clientCrtPath = clientCrtPath;
        }

        public String getClientPkcs8keyPath() {
            return clientPkcs8keyPath;
        }

        public void setClientPkcs8keyPath(String clientPkcs8keyPath) {
            this.clientPkcs8keyPath = clientPkcs8keyPath;
        }
    }
}
