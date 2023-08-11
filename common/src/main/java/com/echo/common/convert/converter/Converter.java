package com.echo.common.convert.converter;

/**
 * 从S类型转成T类型的转换器
 * @param <S>
 * @param <T>
 */
public interface Converter<S, T> {


    /**
     * 类型转换
     * @param source source
     * @return T or null
     */
    T convert(S source);


    /**
     * 提供以流的形式接着转换类型
     * @param after 转换器
     * @return U or null
     * @param <U>
     */
    default <U> Converter<S, U> andThen(Converter<? super T, ? extends U> after) {
        if (after == null) {
            throw new IllegalArgumentException("After Converter must not be null");
        }
        return (S s) -> {
            T initialResult = convert(s);
            return (initialResult != null ? after.convert(initialResult) : null);
        };
    }

}
