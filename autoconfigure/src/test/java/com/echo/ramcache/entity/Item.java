package com.echo.ramcache.entity;

import com.echo.mongo.mapping.anno.Document;

/**
 * @author: li-yuanwen
 */
@Document
public class Item extends AbstractRegionEntity<Long, Long> {


    private int config;
    private int num;

    public Item() {
        super();
    }

    public Item(Long id, Long owner, int config) {
        super(id, owner);
        this.config = config;
        this.num = 1;
    }

    public int getConfig() {
        return config;
    }

    public int getNum() {
        return num;
    }
}
