package com.echo.mongo.index;

import com.echo.common.convert.converter.Converter;
import com.echo.common.util.ObjectUtils;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/**
 * @author: li-yuanwen
 */
public class IndexConverters {

    private static final Converter<IndexDefinition, IndexOptions> DEFINITION_TO_MONGO_INDEX_OPTIONS;
    private static final Converter<Document, IndexInfo> DOCUMENT_INDEX_INFO;

    static {

        DEFINITION_TO_MONGO_INDEX_OPTIONS = getIndexDefinitionIndexOptionsConverter();
        DOCUMENT_INDEX_INFO = getDocumentIndexInfoConverter();
    }

    private IndexConverters() {

    }

    static Converter<IndexDefinition, IndexOptions> indexDefinitionToIndexOptionsConverter() {
        return DEFINITION_TO_MONGO_INDEX_OPTIONS;
    }

    static Converter<Document, IndexInfo> documentToIndexInfoConverter() {
        return DOCUMENT_INDEX_INFO;
    }

    private static Converter<IndexDefinition, IndexOptions> getIndexDefinitionIndexOptionsConverter() {

        return indexDefinition -> {

            Document indexOptions = indexDefinition.getIndexOptions();
            IndexOptions ops = new IndexOptions();

            if (indexOptions.containsKey("name")) {
                ops = ops.name(indexOptions.get("name").toString());
            }
            if (indexOptions.containsKey("unique")) {
                ops = ops.unique((Boolean) indexOptions.get("unique"));
            }
            if (indexOptions.containsKey("sparse")) {
                ops = ops.sparse((Boolean) indexOptions.get("sparse"));
            }
            if (indexOptions.containsKey("background")) {
                ops = ops.background((Boolean) indexOptions.get("background"));
            }
            if (indexOptions.containsKey("expireAfterSeconds")) {
                ops = ops.expireAfter((Long) indexOptions.get("expireAfterSeconds"), TimeUnit.SECONDS);
            }
            if (indexOptions.containsKey("min")) {
                ops = ops.min(((Number) indexOptions.get("min")).doubleValue());
            }
            if (indexOptions.containsKey("max")) {
                ops = ops.max(((Number) indexOptions.get("max")).doubleValue());
            }
            if (indexOptions.containsKey("bits")) {
                ops = ops.bits((Integer) indexOptions.get("bits"));
            }
            if (indexOptions.containsKey("bucketSize")) {
                ops = ops.bucketSize(((Number) indexOptions.get("bucketSize")).doubleValue());
            }
            if (indexOptions.containsKey("default_language")) {
                ops = ops.defaultLanguage(indexOptions.get("default_language").toString());
            }
            if (indexOptions.containsKey("language_override")) {
                ops = ops.languageOverride(indexOptions.get("language_override").toString());
            }
            if (indexOptions.containsKey("weights")) {
                ops = ops.weights((org.bson.Document) indexOptions.get("weights"));
            }

            for (String key : indexOptions.keySet()) {
                if (ObjectUtils.nullSafeEquals("2dsphere", indexOptions.get(key))) {
                    ops = ops.sphereVersion(2);
                }
            }

            if (indexOptions.containsKey("partialFilterExpression")) {
                ops = ops.partialFilterExpression((org.bson.Document) indexOptions.get("partialFilterExpression"));
            }

            if (indexOptions.containsKey("wildcardProjection")) {
                ops.wildcardProjection(indexOptions.get("wildcardProjection", Document.class));
            }

            if (indexOptions.containsKey("hidden")) {
                ops = ops.hidden((Boolean) indexOptions.get("hidden"));
            }

            return ops;
        };
    }


    private static Converter<Document, IndexInfo> getDocumentIndexInfoConverter() {
        return IndexInfo::indexInfoOf;
    }

}
