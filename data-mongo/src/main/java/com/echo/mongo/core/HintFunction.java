package com.echo.mongo.core;

import com.echo.common.util.StringUtils;
import com.echo.mongo.CodecRegistryProvider;
import com.echo.mongo.util.BsonUtils;
import org.bson.conversions.Bson;

import java.util.function.Function;

/**
 * Function object to apply a query hint. Can be an index name or a BSON document.
 */
class HintFunction {

    private static final HintFunction EMPTY = new HintFunction(null);

    private final Object hint;

    private HintFunction(Object hint) {
        this.hint = hint;
    }

    /**
     * Return an empty hint function.
     *
     * @return
     */
    static HintFunction empty() {
        return EMPTY;
    }

    /**
     * Create a {@link HintFunction} from a {@link Bson document} or {@link String index name}.
     *
     * @param hint
     * @return
     */
    static HintFunction from(Object hint) {
        return new HintFunction(hint);
    }

    /**
     * Return whether a hint is present.
     *
     * @return
     */
    public boolean isPresent() {
        return (hint instanceof String && StringUtils.hasLength((String) hint)) || hint instanceof Bson;
    }

    /**
     * If a hint is not present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a hint is not present, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return !isPresent();
    }

    /**
     * Apply the hint to consumers depending on the hint format if {@link #isPresent() present}.
     *
     * @param registryProvider
     * @param stringConsumer
     * @param bsonConsumer
     * @param <R>
     */
    public <R> void ifPresent(CodecRegistryProvider registryProvider, Function<String, R> stringConsumer,
                              Function<Bson, R> bsonConsumer) {

        if (isEmpty()) {
            return;
        }
        apply(registryProvider, stringConsumer, bsonConsumer);
    }

    /**
     * Apply the hint to consumers depending on the hint format.
     *
     * @param registryProvider
     * @param stringConsumer
     * @param bsonConsumer
     * @param <R>
     * @return
     */
    public <R> R apply(CodecRegistryProvider registryProvider, Function<String, R> stringConsumer,
                       Function<Bson, R> bsonConsumer) {

        if (isEmpty()) {
            throw new IllegalStateException("No hint present");
        }

        if (hint instanceof Bson) {
            Bson bson = (Bson) hint;
            return bsonConsumer.apply(bson);
        }

        if (hint instanceof String) {
            String hintString = (String) hint;
            if (BsonUtils.isJsonDocument(hintString)) {
                return bsonConsumer.apply(BsonUtils.parse(hintString, registryProvider));
            }
            return stringConsumer.apply(hintString);
        }

        throw new IllegalStateException(
                "Unable to read hint of type %s" + (hint != null ? hint.getClass() : "null"));
    }


}
