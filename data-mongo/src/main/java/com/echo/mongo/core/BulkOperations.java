package com.echo.mongo.core;


/**
 * mongodb 批量操作
 */
public interface BulkOperations {

    /**
     * Mode for bulk operation.
     **/
    enum BulkMode {

        /** Perform bulk operations in sequence. The first error will cancel processing. */
        ORDERED,

        /** Perform bulk operations in parallel. Processing will continue on errors. */
        UNORDERED
    };

}
