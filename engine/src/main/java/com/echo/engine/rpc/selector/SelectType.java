package com.echo.engine.rpc.selector;

public enum SelectType {


    /**
     * 身份标识选择器
     **/
    IDENTITY_SELECTOR(new IdentityServerSelector()),
    /**
     * 负载均衡选择器
     **/
    BALANCE_SELECTOR(new BalanceServerSelector()),
    /**
     * 标识哈希选择器
     **/
    HASH_SELECTOR(new HashServerSelector()),
    /**
     * 指定机器选择器
     **/
    SPECIFIC_SELECTOR(new SpecificSelector()),
    ;

    private final ServerSelector<?> selector;

    SelectType(ServerSelector<?> selector) {
        this.selector = selector;
    }

    public ServerSelector<Object> getSelector() {
        return (ServerSelector<Object>) selector;
    }
}
