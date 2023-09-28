package com.echo.common.id;

public interface DistributedIdGenerator extends IdGenerator {

    int getWorkerId(long id);

}
