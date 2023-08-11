package com.echo.common.logging.message;

/**
 * 抽象玩家通用埋点日志基类
 * @author li-yuanwen
 * @date 2022/3/30
 */
public abstract class AbstractUserRecordMessage implements RecordMessage {

    private static final long serialVersionUID = -4270169979652992894L;


    @Override
    public String getFormattedMessage() {
        return format();
    }

    @Override
    public String getFormat() {
        return format();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
