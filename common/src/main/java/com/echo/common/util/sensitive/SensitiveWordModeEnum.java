package com.echo.common.util.sensitive;

/**
 * 敏感词影响范围
 */
public enum SensitiveWordModeEnum {

    SHIELD("SHIELD", "屏蔽"),

    DST("DST", "脱敏"),

    WARN("WARN", "警告"),

    ;

    private final String code;
    private final String dec;

    SensitiveWordModeEnum(String code, String dec) {
        this.code = code;
        this.dec = dec;
    }

    public String getCode() {
        return code;
    }

    public String getDec() {
        return dec;
    }
}
