package com.echo.common.logging.message;

import org.apache.logging.log4j.message.Message;

/**
 * 通用日志消息接口
 * @author li-yuanwen
 * @date 2022/3/29
 */
public interface RecordMessage extends Message {

    /**
     * 获取日志名称
     * @return 日志名称
     */
    default String getRecordName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 日志消息内容格式化输出
     * @return 日志消息内容格式化输出
     */
    String format();

}
