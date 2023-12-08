package com.echo;

import java.util.Objects;

/**
 * @author: li-yuanwen
 */
public class Message {

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(msg, message.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg);
    }
}
