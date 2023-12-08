package com.echo.ioc.context;

public interface ApplicationCloseListener {


    default int getOrder() {
        return Integer.MAX_VALUE;
    }


    void applicationClose();

}
