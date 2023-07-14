package com.echo.ioc.prop;

import cn.hutool.core.convert.Convert;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于PropertySources的属性容器
 */
@Slf4j
public class PropertySourcesPropertyResolver implements PropertyResolver {

    private final PropertySources propertySources;


    public PropertySourcesPropertyResolver() {
        this(new MutablePropertySources());
    }

    /**
     * Create a new resolver against the given property sources.
     * @param propertySources the set of {@link PropertySource} objects to use
     */
    public PropertySourcesPropertyResolver(PropertySources propertySources) {
        this.propertySources = propertySources;
    }

    @Override
    public boolean containsProperty(String key) {
        if (this.propertySources != null) {
            for (PropertySource<?> propertySource : this.propertySources) {
                if (propertySource.containsProperty(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getProperty(key, String.class, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        if (this.propertySources != null) {
            for (PropertySource<?> propertySource : this.propertySources) {
                if (log.isTraceEnabled()) {
                    log.trace("Searching for key '" + key + "' in PropertySource '" +
                            propertySource.getName() + "'");
                }
                Object value = propertySource.getProperty(key);
                if (value != null) {
                    return Convert.convert(targetType, value);
                } else {
                    return defaultValue;
                }
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Could not find key '" + key + "' in any property source");
        }
        return defaultValue;
    }

}
