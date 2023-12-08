package com.echo.ramcache.entity;

/**
 * 数据状态
 *
 * @author li-yuanwen
 * @date 2022/1/25
 */
public enum DataStatus {

    /**
     * 从db里面加载出来的数据，无需处理
     */
    INIT(0),

    /**
     * 新创建的的对象
     */
    NEW(1),

    /**
     * 对象被修改
     */
    MODIFY(2),

    /**
     * 需要删除，先删db以后再移除内存
     */
    DELETE(3),

    ;

    private int code;

    DataStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
