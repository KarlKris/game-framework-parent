package com.echo.common.util;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Lazy implementation of Streamable obtains a Stream from a given Supplier.
 * @author: li-yuanwen
 */
public class LazyStreamable<T> implements Streamable<T> {

    private final Supplier<? extends Stream<T>> stream;

    private LazyStreamable(Supplier<? extends Stream<T>> stream) {
        this.stream = stream;
    }

    public static <T> LazyStreamable<T> of(Supplier<? extends Stream<T>> stream) {
        return new LazyStreamable<T>(stream);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<T> stream() {
        return stream.get();
    }

    public Supplier<? extends Stream<T>> getStream() {
        return this.stream;
    }

    @Override
    public String toString() {
        return "LazyStreamable(stream=" + this.getStream() + ")";
    }
}
