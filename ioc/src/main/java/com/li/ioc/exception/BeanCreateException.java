package com.li.ioc.exception;

/**
 * Bean单例创建异常
 * @author li-yuanwen
 * @date 2023/3/25
 */
public class BeanCreateException extends RuntimeException {

    private String beanName;

    public BeanCreateException(String beanName, String message) {
        super(message);
        this.beanName = beanName;
    }

    public BeanCreateException(String beanName, String message, Throwable cause) {
        super(message, cause);
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
