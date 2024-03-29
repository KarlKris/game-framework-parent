package com.echo.ioc.prop;

import com.echo.common.util.StringUtils;

import java.util.Map;

/**
 * 基于Map存储属性的PropertySource
 */
public class MapPropertySource extends PropertySource<Map<String, String>> {

    public MapPropertySource(String name, Map<String, String> source) {
        super(name, source);
    }

    @Override
    public boolean containsProperty(String name) {
        return getSource().containsKey(name);
    }

    @Override
    public String getProperty(String name) {
        return getSource().get(name);
    }

    public String[] getPropertyNames() {
        return StringUtils.toStringArray(getSource().keySet());
    }
}
