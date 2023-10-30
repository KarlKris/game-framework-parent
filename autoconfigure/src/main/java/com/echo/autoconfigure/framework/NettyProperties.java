package com.echo.autoconfigure.framework;

import com.echo.ioc.anno.ConfigurationProperties;
import com.echo.network.serialize.SerializeType;

/**
 * Netty服务相关配置
 *
 * @author: li-yuanwen
 */
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

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

    // ----------- ssl ----------------
    /**
     * sll协议
     **/
    private String sslProtocol;
    /**
     * ca证书地址
     **/
    private String sslCaPath;

    /**
     * 服务端crt path
     **/
    private String sslServerCrtPath;
    /**
     * 服务端ssl 秘钥pkcs#8编码
     **/
    private String sslServerPkcs8keyPath;

    /**
     * 客户端crt path
     **/
    private String sslClientCrtPath;
    /**
     * 客户端ssl 秘钥pkcs#8编码
     **/
    private String sslClientPkcs8keyPath;

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

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getSslCaPath() {
        return sslCaPath;
    }

    public void setSslCaPath(String sslCaPath) {
        this.sslCaPath = sslCaPath;
    }

    public String getSslServerCrtPath() {
        return sslServerCrtPath;
    }

    public void setSslServerCrtPath(String sslServerCrtPath) {
        this.sslServerCrtPath = sslServerCrtPath;
    }

    public String getSslServerPkcs8keyPath() {
        return sslServerPkcs8keyPath;
    }

    public void setSslServerPkcs8keyPath(String sslServerPkcs8keyPath) {
        this.sslServerPkcs8keyPath = sslServerPkcs8keyPath;
    }

    public String getSslClientCrtPath() {
        return sslClientCrtPath;
    }

    public void setSslClientCrtPath(String sslClientCrtPath) {
        this.sslClientCrtPath = sslClientCrtPath;
    }

    public String getSslClientPkcs8keyPath() {
        return sslClientPkcs8keyPath;
    }

    public void setSslClientPkcs8keyPath(String sslClientPkcs8keyPath) {
        this.sslClientPkcs8keyPath = sslClientPkcs8keyPath;
    }

    public String getProtocolPackage() {
        return protocolPackage;
    }

    public void setProtocolPackage(String protocolPackage) {
        this.protocolPackage = protocolPackage;
    }
}
