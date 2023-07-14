package com.echo.ioc.prop;

import java.util.Objects;

/**
 * 表存储属性的 key-value对的源容器,value可以是任何类型
 */
public abstract class PropertySource<T> {

    /** key **/
    private final String name;
    private final T source;

    public PropertySource(String name) {
        this(name, (T) new Object());
    }

    public PropertySource(String name, T source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public T getSource() {
        return source;
    }

    /**
     * 判断是否存在指定name的属性值
     * @param name 属性名称
     * @return true 容器中存在
     */
    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    /**
     * 获取容器中指定属性名称的属性值
     * @param name 属性名称
     * @return null or 属性值
     */
    public abstract Object getProperty(String name);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertySource<?> that = (PropertySource<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static PropertySource<?> named(String name) {
        return new PropertySource<Object>(name) {
            @Override
            public Object getProperty(String name) {
                throw new UnsupportedOperationException("PropertySource.named() instances are for use with collection comparison only");
            }
        };
    }
}
