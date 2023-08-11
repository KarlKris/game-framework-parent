package com.echo.mongo.mapping;

import com.echo.common.util.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * class容器
 * @author: li-yuanwen
 */
public interface ManagedTypes {

    /**
     * Factory method used to construct a new instance of {@link ManagedTypes} containing no {@link Class types}.
     *
     * @return an empty {@link ManagedTypes} instance.
     * @see java.util.Collections#emptySet()
     * @see #fromIterable(Iterable)
     */
    static ManagedTypes empty() {
        return fromIterable(Collections.emptySet());
    }

    /**
     * Factory method used to construct {@link ManagedTypes} from the given array of {@link Class types}.
     *
     * @param types array of {@link Class types} used to initialize the {@link ManagedTypes}; must not be {@literal null}.
     * @return new instance of {@link ManagedTypes} initialized from {@link Class types}.
     */
    static ManagedTypes from(Class<?>... types) {
        return fromIterable(Arrays.asList(types));
    }

    /**
     * Factory method used to construct {@link ManagedTypes} from the given, required {@link Iterable} of {@link Class
     * types}.
     *
     * @param types {@link Iterable} of {@link Class types} used to initialize the {@link ManagedTypes}; must not be
     *          {@literal null}.
     * @return new instance of {@link ManagedTypes} initialized the given, required {@link Iterable} of {@link Class
     *         types}.
     * @see java.lang.Iterable
     * @see #fromStream(Stream)
     * @see #fromSupplier(Supplier)
     */
    static ManagedTypes fromIterable(Iterable<? extends Class<?>> types) {

        if (types == null) {
            throw new IllegalArgumentException("Types must not be null");
        }
        return types::forEach;
    }

    /**
     * Factory method used to construct {@link ManagedTypes} from the given, required {@link Stream} of {@link Class
     * types}.
     *
     * @param types {@link Stream} of {@link Class types} used to initialize the {@link ManagedTypes}; must not be
     *          {@literal null}.
     * @return new instance of {@link ManagedTypes} initialized the given, required {@link Stream} of {@link Class types}.
     * @see java.util.stream.Stream
     * @see #fromIterable(Iterable)
     * @see #fromSupplier(Supplier)
     */
    static ManagedTypes fromStream(Stream<? extends Class<?>> types) {

        if (types == null) {
            throw new IllegalArgumentException("Types must not be null");
        }

        return types::forEach;
    }

    /**
     * Factory method used to construct {@link ManagedTypes} from the given, required {@link Supplier} of an
     * {@link Iterable} of {@link Class types}.
     *
     * @param dataProvider {@link Supplier} of an {@link Iterable} of {@link Class types} used to lazily initialize the
     *          {@link ManagedTypes}; must not be {@literal null}.
     * @return new instance of {@link ManagedTypes} initialized the given, required {@link Supplier} of an
     *         {@link Iterable} of {@link Class types}.
     * @see java.util.function.Supplier
     * @see java.lang.Iterable
     * @see #fromIterable(Iterable)
     * @see #fromStream(Stream)
     */
    static ManagedTypes fromSupplier(Supplier<Iterable<Class<?>>> dataProvider) {

        if (dataProvider == null) {
            throw new IllegalArgumentException("Supplier must not be null");
        }

        return new ManagedTypes() {

            final Lazy<Iterable<Class<?>>> lazyProvider = Lazy.of(dataProvider);

            @Override
            public void forEach(Consumer<Class<?>> action) {
                lazyProvider.get().forEach(action);
            }
        };
    }

    /**
     * Applies the given {@link Consumer action} to each of the {@link Class types} contained in this {@link ManagedTypes}
     * instance.
     *
     * @param action {@link Consumer} defining the action to perform on the {@link Class types} contained in this
     *          {@link ManagedTypes} instance; must not be {@literal null}.
     * @see java.util.function.Consumer
     */
    void forEach(Consumer<Class<?>> action);

    /**
     * Returns all the {@link ManagedTypes} in a {@link List}.
     *
     * @return these {@link ManagedTypes} in a {@link List}; never {@literal null}.
     * @see java.util.List
     */
    default List<Class<?>> toList() {

        List<Class<?>> list = new ArrayList<>(100);
        forEach(list::add);
        return list;
    }
}
