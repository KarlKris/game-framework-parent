package com.echo.ioc.prop;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * PropertySources接口的默认实现。允许操作包含的属性源，并提供用于复制现有PropertySources实例的构造函数。
 */
public class MutablePropertySources implements PropertySources {

    private final List<PropertySource<?>> propertySources = new CopyOnWriteArrayList<>();

    /**
     * Create a new {@link MutablePropertySources} object.
     */
    public MutablePropertySources() {
    }

    /**
     * Create a new {@code MutablePropertySources} from the given propertySources
     * object, preserving the original order of contained {@code PropertySource} objects.
     */
    public MutablePropertySources(PropertySources propertySources) {
        this();
        for (PropertySource<?> propertySource : propertySources) {
            addLast(propertySource);
        }
    }


    @Override
    public Iterator<PropertySource<?>> iterator() {
        return this.propertySources.iterator();
    }

    @Override
    public Spliterator<PropertySource<?>> spliterator() {
        return Spliterators.spliterator(this.propertySources, 0);
    }

    @Override
    public Stream<PropertySource<?>> stream() {
        return this.propertySources.stream();
    }

    @Override
    public boolean contains(String name) {
        for (PropertySource<?> propertySource : this.propertySources) {
            if (propertySource.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PropertySource<?> get(String name) {
        for (PropertySource<?> propertySource : this.propertySources) {
            if (propertySource.getName().equals(name)) {
                return propertySource;
            }
        }
        return null;
    }

    @Override
    public Object getProperty(String propertyName) {
        Object value = null;
        for (PropertySource<?> propertySource : this.propertySources) {
            if ((value = propertySource.getProperty(propertyName)) != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Add the given property source object with highest precedence.
     */
    public void addFirst(PropertySource<?> propertySource) {
        synchronized (this.propertySources) {
            removeIfPresent(propertySource);
            this.propertySources.add(0, propertySource);
        }
    }

    /**
     * Add the given property source object with lowest precedence.
     */
    public void addLast(PropertySource<?> propertySource) {
        synchronized (this.propertySources) {
            removeIfPresent(propertySource);
            this.propertySources.add(propertySource);
        }
    }

    /**
     * Add the given property source object with precedence immediately higher
     * than the named relative property source.
     */
    public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        synchronized (this.propertySources) {
            removeIfPresent(propertySource);
            int index = assertPresentAndGetIndex(relativePropertySourceName);
            addAtIndex(index, propertySource);
        }
    }

    /**
     * Add the given property source object with precedence immediately lower
     * than the named relative property source.
     */
    public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        synchronized (this.propertySources) {
            removeIfPresent(propertySource);
            int index = assertPresentAndGetIndex(relativePropertySourceName);
            addAtIndex(index + 1, propertySource);
        }
    }

    /**
     * Return the precedence of the given property source, {@code -1} if not found.
     */
    public int precedenceOf(PropertySource<?> propertySource) {
        return this.propertySources.indexOf(propertySource);
    }

    /**
     * Remove and return the property source with the given name, {@code null} if not found.
     * @param name the name of the property source to find and remove
     */
    public PropertySource<?> remove(String name) {
        synchronized (this.propertySources) {
            int index = this.propertySources.indexOf(PropertySource.named(name));
            return (index != -1 ? this.propertySources.remove(index) : null);
        }
    }

    /**
     * Replace the property source with the given name with the given property source object.
     * @param name the name of the property source to find and replace
     * @param propertySource the replacement property source
     * @throws IllegalArgumentException if no property source with the given name is present
     * @see #contains
     */
    public void replace(String name, PropertySource<?> propertySource) {
        synchronized (this.propertySources) {
            int index = assertPresentAndGetIndex(name);
            this.propertySources.set(index, propertySource);
        }
    }

    /**
     * Return the number of {@link PropertySource} objects contained.
     */
    public int size() {
        return this.propertySources.size();
    }

    @Override
    public String toString() {
        return this.propertySources.toString();
    }


    /**
     * Ensure that the given property source is not being added relative to itself.
     */
    protected void assertLegalRelativeAddition(String relativePropertySourceName, PropertySource<?> propertySource) {
        String newPropertySourceName = propertySource.getName();
        if (relativePropertySourceName.equals(newPropertySourceName)) {
            throw new IllegalArgumentException(
                    "PropertySource named '" + newPropertySourceName + "' cannot be added relative to itself");
        }
    }

    /**
     * Remove the given property source if it is present.
     */
    protected void removeIfPresent(PropertySource<?> propertySource) {
        this.propertySources.remove(propertySource);
    }

    /**
     * Add the given property source at a particular index in the list.
     */
    private void addAtIndex(int index, PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySources.add(index, propertySource);
    }

    /**
     * Assert that the named property source is present and return its index.
     * @param name {@linkplain PropertySource#getName() name of the property source} to find
     * @throws IllegalArgumentException if the named property source is not present
     */
    private int assertPresentAndGetIndex(String name) {
        int index = this.propertySources.indexOf(PropertySource.named(name));
        if (index == -1) {
            throw new IllegalArgumentException("PropertySource named '" + name + "' does not exist");
        }
        return index;
    }
}
