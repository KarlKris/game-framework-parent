package com.echo.mongo.index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注索引
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {

    /**
     * If set to true reject all documents that contain a duplicate value for the indexed field.
     *
     * @return {@literal false} by default.
     * @see <a href=
     * "https://docs.mongodb.org/manual/core/index-unique/">https://docs.mongodb.org/manual/core/index-unique/</a>
     */
    boolean unique() default false;

    /**
     * The index sort direction.
     *
     * @return {@link IndexDirection#ASCENDING} by default.
     */
    IndexDirection direction() default IndexDirection.ASCENDING;

    /**
     * If set to true index will skip over any document that is missing the indexed field. <br />
     * Must not be used with {@link #partialFilter()}.
     *
     * @return {@literal false} by default.
     * @see <a href=
     * "https://docs.mongodb.org/manual/core/index-sparse/">https://docs.mongodb.org/manual/core/index-sparse/</a>
     */
    boolean sparse() default false;

    /**
     * Index name
     * <br />
     * The name will only be applied as is when defined on root level. For usage on nested or embedded structures the
     * provided name will be prefixed with the path leading to the entity. <br />
     * <br />
     * The structure below
     *
     * <pre>
     * <code>
     * &#64;Document
     * class Root {
     *   Hybrid hybrid;
     *   Nested nested;
     * }
     *
     * &#64;Document
     * class Hybrid {
     *   &#64;Indexed(name="index") String h1;
     *   &#64;Indexed(name="#{&#64;myBean.indexName}") String h2;
     * }
     *
     * class Nested {
     *   &#64;Indexed(name="index") String n1;
     * }
     * </code>
     * </pre>
     * <p>
     * resolves in the following index structures
     *
     * <pre>
     * <code>
     * db.root.createIndex( { hybrid.h1: 1 } , { name: "hybrid.index" } )
     * db.root.createIndex( { nested.n1: 1 } , { name: "nested.index" } )
     * db.hybrid.createIndex( { h1: 1} , { name: "index" } )
     * db.hybrid.createIndex( { h2: 1} , { name: the value myBean.getIndexName() returned } )
     * </code>
     * </pre>
     *
     * @return empty String by default.
     */
    String name() default "";

    /**
     * If set to {@literal true} then MongoDB will ignore the given index name and instead generate a new name. Defaults
     * to {@literal false}.
     *
     * @return {@literal false} by default.
     * @since 1.5
     */
    boolean useGeneratedName() default false;

    /**
     * If {@literal true} the index will be created in the background.
     *
     * @return {@literal false} by default.
     * @see <a href=
     * "https://docs.mongodb.org/manual/core/indexes/#background-construction">https://docs.mongodb.org/manual/core/indexes/#background-construction</a>
     */
    boolean background() default false;

    /**
     * Configures the number of seconds after which the collection should expire. Defaults to -1 for no expiry.
     *
     * @return {@literal -1} by default.
     * @see <a href=
     * "https://docs.mongodb.org/manual/tutorial/expire-data/">https://docs.mongodb.org/manual/tutorial/expire-data/</a>
     */
    int expireAfterSeconds() default -1;


    /**
     * Only index the documents in a collection that meet a specified {@link IndexFilter filter expression}. <br />
     * Must not be used with {@link #sparse() sparse = true}.
     *
     * @return empty by default.
     * @see <a href=
     * "https://docs.mongodb.com/manual/core/index-partial/">https://docs.mongodb.com/manual/core/index-partial/</a>
     * @since 3.1
     */
    String partialFilter() default "";

}
