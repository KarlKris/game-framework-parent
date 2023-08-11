package com.echo.common.logging.message;

/**
 * 抽象服务器通用埋点日志基类
 * @author li-yuanwen
 * @date 2022/3/30
 */
public abstract class AbstractServerRecordMessage implements RecordMessage {

    private static final long serialVersionUID = 3285253251126925196L;

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
