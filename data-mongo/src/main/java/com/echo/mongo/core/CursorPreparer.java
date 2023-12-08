package com.echo.mongo.core;

import com.mongodb.client.FindIterable;
import org.bson.Document;

public interface CursorPreparer {

    /**
     * Default {@link CursorPreparer} just passing on the given {@link FindIterable}.
     *
     * @since 2.2
     */
    CursorPreparer NO_OP_PREPARER = (iterable -> iterable);

    /**
     * Prepare the given cursor (apply limits, skips and so on). Returns the prepared cursor.
     *
     * @param iterable must not be {@literal null}.
     * @return never {@literal null}.
     */
    FindIterable<Document> prepare(FindIterable<Document> iterable);

}
