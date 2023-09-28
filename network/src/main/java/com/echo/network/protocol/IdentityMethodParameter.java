package com.echo.network.protocol;

/**
 * 身份标识参数,用@Identity注解装饰的参数
 *
 * @author li-yuanwen
 * @date 2021/7/31 15:11
 **/
public class IdentityMethodParameter extends AbstractMethodParameter {

    public static final IdentityMethodParameter IDENTITY_PARAMETER = new IdentityMethodParameter();

    public IdentityMethodParameter() {
        super(Long.class);
    }
}
