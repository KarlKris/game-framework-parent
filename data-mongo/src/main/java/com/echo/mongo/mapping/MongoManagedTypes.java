package com.echo.mongo.mapping;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * mongodb 对应实体 class 容器
 * @author: li-yuanwen
 */
public class MongoManagedTypes implements ManagedTypes {

    private final ManagedTypes delegate;

    private MongoManagedTypes(ManagedTypes types) {
        this.delegate = types;
    }

    /**
     * Wraps an existing {@link ManagedTypes} object with {@link MongoManagedTypes}.
     *
     * @param managedTypes
     * @return
     */
    public static MongoManagedTypes from(ManagedTypes managedTypes) {
        return new MongoManagedTypes(managedTypes);
    }

    /**
     * Factory method used to construct {@link MongoManagedTypes} from the given array of {@link Class types}.
     *
     * @param types array of {@link Class types} used to initialize the {@link ManagedTypes}; must not be {@literal null}.
     * @return new instance of {@link MongoManagedTypes} initialized from {@link Class types}.
     */
    public static MongoManagedTypes from(Class<?>... types) {
        return fromIterable(Arrays.asList(types));
    }

    /**
     * Factory method used to construct {@link MongoManagedTypes} from the given, required {@link Iterable} of
     * {@link Class types}.
     *
     * @param types {@link Iterable} of {@link Class types} used to initialize the {@link ManagedTypes}; must not be
     *          {@literal null}.
     * @return new instance of {@link MongoManagedTypes} initialized the given, required {@link Iterable} of {@link Class
     *         types}.
     */
    public static MongoManagedTypes fromIterable(Iterable<? extends Class<?>> types) {
        return from(ManagedTypes.fromIterable(types));
    }

    /**
     * Factory method to return an empty {@link MongoManagedTypes} object.
     *
     * @return an empty {@link MongoManagedTypes} object.
     */
    public static MongoManagedTypes empty() {
        return from(ManagedTypes.empty());
    }

    @Override
    public void forEach(Consumer<Class<?>> action) {
        delegate.forEach(action);
    }

}
