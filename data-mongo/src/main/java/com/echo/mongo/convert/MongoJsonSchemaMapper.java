package com.echo.mongo.convert;

import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.echo.mongo.mapping.MongoPersistentProperty;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * mongodb @$jsonSchema 运算符
 * @author: li-yuanwen
 */
public class MongoJsonSchemaMapper {

    private static final String $JSON_SCHEMA = "$jsonSchema";
    private static final String REQUIRED_FIELD = "required";
    private static final String PROPERTIES_FIELD = "properties";
    private static final String ENUM_FIELD = "enum";

    private final MongoMappingContext mappingContext;
    private final MongoConverter converter;

    public MongoJsonSchemaMapper(MongoMappingContext mappingContext, MongoConverter converter) {
        this.mappingContext = mappingContext;
        this.converter = converter;
    }

    public Document mapSchema(Document jsonSchema, Class<?> type) {

        if (jsonSchema.containsKey($JSON_SCHEMA)) {
            throw new UnsupportedOperationException(String.format("Document does not contain $jsonSchema field; Found: %s", jsonSchema));
        }

        if (Object.class.equals(type)) {
            return new Document(jsonSchema);
        }

        return new Document($JSON_SCHEMA,
                mapSchemaObject(mappingContext.getPersistentEntity(type), jsonSchema.get($JSON_SCHEMA, Document.class)));
    }

    @SuppressWarnings("unchecked")
    private Document mapSchemaObject(MongoPersistentEntity entity, Document source) {

        Document sink = new Document(source);

        if (source.containsKey(REQUIRED_FIELD)) {
            sink.replace(REQUIRED_FIELD, mapRequiredProperties(entity, source.get(REQUIRED_FIELD, Collection.class)));
        }

        if (source.containsKey(PROPERTIES_FIELD)) {
            sink.replace(PROPERTIES_FIELD, mapProperties(entity, source.get(PROPERTIES_FIELD, Document.class)));
        }

        mapEnumValuesIfNecessary(sink);

        return sink;
    }

    private Document mapProperties(MongoPersistentEntity entity, Document source) {

        Document sink = new Document();
        for (String fieldName : source.keySet()) {

            String mappedFieldName = getFieldName(entity, fieldName);
            Document mappedProperty = mapProperty(entity, fieldName, source.get(fieldName, Document.class));

            sink.append(mappedFieldName, mappedProperty);
        }
        return sink;
    }

    private List<String> mapRequiredProperties(MongoPersistentEntity entity,
                                               Collection<String> sourceFields) {

        return sourceFields.stream() ///
                .map(fieldName -> getFieldName(entity, fieldName)) //
                .collect(Collectors.toList());
    }

    private Document mapProperty(MongoPersistentEntity entity, String sourceFieldName,
                                 Document source) {

        Document sink = new Document(source);
        return mapEnumValuesIfNecessary(sink);
    }

    private Document mapEnumValuesIfNecessary(Document source) {

        Document sink = new Document(source);
        if (source.containsKey(ENUM_FIELD)) {
            sink.replace(ENUM_FIELD, mapEnumValues(source.get(ENUM_FIELD, Iterable.class)));
        }
        return sink;
    }

    private List<Object> mapEnumValues(Iterable<?> values) {

        List<Object> converted = new ArrayList<>();
        for (Object val : values) {
            converted.add(converter.convertToMongoType(val));
        }
        return converted;
    }

    private String getFieldName(MongoPersistentEntity entity, String sourceField) {

        if (entity == null) {
            return sourceField;
        }

        MongoPersistentProperty property = entity.getPersistentProperty(sourceField);
        return property != null ? property.getFieldName() : sourceField;
    }
}
