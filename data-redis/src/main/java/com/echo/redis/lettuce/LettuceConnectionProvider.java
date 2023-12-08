package com.echo.redis.lettuce;

import io.lettuce.core.api.StatefulConnection;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface LettuceConnectionProvider {


    /**
     * Request a connection given {@code connectionType}. Providing a connection type allows specialization to provide a
     * more specific connection type.
     *
     * @param connectionType must not be {@literal null}.
     * @return the requested connection. Must be {@link #release(StatefulConnection) released} if the connection is no
     * longer in use.
     */
    default <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {
        return join(getConnectionAsync(connectionType));
    }

    /**
     * Request asynchronously a connection given {@code connectionType}. Providing a connection type allows specialization
     * to provide a more specific connection type.
     *
     * @param connectionType must not be {@literal null}.
     * @return a {@link CompletionStage} that is notified with the connection progress. Must be
     * {@link #releaseAsync(StatefulConnection) released} if the connection is no longer in use.
     * @since 2.2
     */
    <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType);


    /**
     * Release the {@link StatefulConnection connection}. Closes connection {@link StatefulConnection#close()} by default.
     * Implementations may choose whether they override this method and return the connection to a pool.
     *
     * @param connection must not be {@literal null}.
     */
    default void release(StatefulConnection<?, ?> connection) {
        join(releaseAsync(connection));
    }

    /**
     * Release asynchronously the {@link StatefulConnection connection}. Closes connection
     * {@link StatefulConnection#closeAsync()} by default. Implementations may choose whether they override this method
     * and return the connection to a pool.
     *
     * @param connection must not be {@literal null}.
     * @return Close {@link CompletableFuture future} notified once the connection is released.
     * @since 2.2
     */
    default CompletableFuture<Void> releaseAsync(StatefulConnection<?, ?> connection) {
        return connection.closeAsync();
    }


    /**
     * destroy connection
     **/
    default void destroy() throws Exception {

    }


    static <T> T join(CompletionStage<T> future) throws RuntimeException, CompletionException {

        if (future == null) {
            throw new IllegalArgumentException("CompletableFuture must not be null!");
        }

        try {
            return future.toCompletableFuture().join();
        } catch (Exception e) {

            Throwable exceptionToUse = e;

            if (e instanceof CompletionException) {
                exceptionToUse = e.getCause();
            }

            if (exceptionToUse instanceof RuntimeException) {
                throw (RuntimeException) exceptionToUse;
            }

            throw new CompletionException(exceptionToUse);
        }
    }


    static <T> CompletableFuture<T> failed(Throwable throwable) {

        if (throwable == null) {
            throw new IllegalArgumentException("Throwable must not be null!");
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);

        return future;
    }

    static <T> Function<Throwable, T> ignoreErrors() {
        return ignored -> null;
    }

}
