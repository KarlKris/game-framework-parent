package com.echo.ramcache.entity;

import com.echo.mongo.mapping.anno.Document;

/**
 * @author: li-yuanwen
 */
@Document
public class Player extends AbstractEntity<Long> {


    private String name;

    public Player() {
        super();
    }

    public Player(Long id, String name) {
        super(id);
        this.name = name;
    }

    @Commit
    public void modify(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
