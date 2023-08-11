package com.echo.common.util;

import cn.hutool.core.lang.Assert;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

public interface StreamUtils {

    /**
     * Returns a {@link Stream} backed by the given {@link Iterator}
     *
     * @param iterator must not be {@literal null}.
     * @return
     */
    static <T> Stream<T> createStreamFromIterator(Iterator<T> iterator) {

        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Returns a {@link Collector} to create an unmodifiable {@link List}.
     *
     * @return will never be {@literal null}.
     */
    static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return collectingAndThen(toList(), Collections::unmodifiableList);
    }

    /**
     * Returns a {@link Collector} to create an unmodifiable {@link Set}.
     *
     * @return will never be {@literal null}.
     */
    static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return collectingAndThen(toSet(), Collections::unmodifiableSet);
    }

    /**
     * Creates a new {@link Stream} for the given value returning an empty {@link Stream} if the value is {@literal null}.
     *
     * @param source can be {@literal null}.
     * @return a new {@link Stream} for the given value returning an empty {@link Stream} if the value is {@literal null}.
     * @since 2.0.6
     */
    static <T> Stream<T> fromNullable(T source) {
        return source == null ? Stream.empty() : Stream.of(source);
    }

    /**
     * Zips the given {@link Stream}s using the given {@link BiFunction}. The resulting {@link Stream} will have the
     * length of the shorter of the two, abbreviating the zipping when the shorter of the two {@link Stream}s is
     * exhausted.
     *
     * @param left must not be {@literal null}.
     * @param right must not be {@literal null}.
     * @param combiner must not be {@literal null}.
     * @return
     * @since 2.1
     */
    static <L, R, T> Stream<T> zip(Stream<L> left, Stream<R> right, BiFunction<L, R, T> combiner) {

        Assert.notNull(left, "Left stream must not be null");
        Assert.notNull(right, "Right must not be null");
        Assert.notNull(combiner, "Combiner must not be null");

        Spliterator<L> lefts = left.spliterator();
        Spliterator<R> rights = right.spliterator();

        long size = Long.min(lefts.estimateSize(), rights.estimateSize());
        int characteristics = lefts.characteristics() & rights.characteristics();
        boolean parallel = left.isParallel() || right.isParallel();

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(size, characteristics) {

            @Override
            @SuppressWarnings("null")
            public boolean tryAdvance(Consumer<? super T> action) {

                Sink<L> leftSink = new Sink<L>();
                Sink<R> rightSink = new Sink<R>();

                boolean leftAdvance = lefts.tryAdvance(leftSink);

                if (!leftAdvance) {
                    return false;
                }

                boolean rightAdvance = rights.tryAdvance(rightSink);

                if (!rightAdvance) {
                    return false;
                }

                action.accept(combiner.apply(leftSink.getValue(), rightSink.getValue()));

                return true;
            }
        }, parallel);
    }
}
