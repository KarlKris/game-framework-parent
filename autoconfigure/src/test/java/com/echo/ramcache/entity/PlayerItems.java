package com.echo.ramcache.entity;

import java.util.List;

/**
 * @author: li-yuanwen
 */
public class PlayerItems extends AbstractRegionEntityContext<Long, Long, Item> {


    public PlayerItems(long owner, List<Item> items) {
        super(owner, items);
    }


}
