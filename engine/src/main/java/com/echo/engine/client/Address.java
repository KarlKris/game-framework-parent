package com.echo.engine.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author li-yuanwen
 * @date 2021/8/3 22:30
 * IP地址
 **/
@Getter
@AllArgsConstructor
public class Address {

    /**
     * Ip地址
     **/
    private final String ip;
    /**
     * 端口
     **/
    private final int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }

        Address address = (Address) o;

        if (port != address.port) {
            return false;
        }
        return ip.equals(address.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
