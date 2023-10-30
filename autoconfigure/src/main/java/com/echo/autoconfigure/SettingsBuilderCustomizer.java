package com.echo.autoconfigure;

/**
 * 配置对象映射，实现XXXProperties -> XXXSettings 的构建
 *
 * @param <SB>
 */
public interface SettingsBuilderCustomizer<SB> {


    void customize(SB builder);

}
