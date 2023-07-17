package com.echo.resources;

import com.echo.common.resource.anno.ResourceId;
import com.echo.common.resource.anno.ResourceObj;

/**
 * @author: li-yuanwen
 */
@ResourceObj("resources")
public class TestSetting {

    @ResourceId
    private int id;
    private int x;
    private int y;

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
