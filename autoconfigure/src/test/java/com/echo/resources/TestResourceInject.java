package com.echo.resources;

import com.echo.autoconfigure.resources.ResourceInject;
import com.echo.common.resource.storage.ResourceStorage;
import com.echo.ioc.anno.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * @author: li-yuanwen
 */
@Slf4j
@Component
public class TestResourceInject {

    @ResourceInject
    public ResourceStorage<Integer, TestSetting> storage;

    @PostConstruct
    public void init() {
        storage.addListener(storage -> print());
    }

    public void print() {
        StringBuilder builder = new StringBuilder();
        for (TestSetting setting : storage.getAll()) {
            builder.append(setting.getId()).append(":").append(setting.getX()).append(":").append(setting.getY()).append("|");
        }
        log.info(builder.toString());
    }

}
