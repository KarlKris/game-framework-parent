package com.echo.mongo.query;

import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * field projection
 * @author: li-yuanwen
 */
public class Fields {


    private final Map<String, Object> criteria = new HashMap<>(8);
    private final Map<String, Object> slices = new HashMap<>(4);
    private final Map<String, Criteria> elemMatchs = new HashMap<>(4);
    private String positionKey;
    private int positionValue;

    public Document getFieldsObject() {
        Document document = new Document(criteria);
        for (Map.Entry<String, Object> entry : slices.entrySet()) {
            document.put(entry.getKey(), new Document("$slice", entry.getValue()));
        }
        for (Map.Entry<String, Criteria> entry : elemMatchs.entrySet()) {
            document.put(entry.getKey(), new Document("$elemMatch", entry.getValue().getCriteriaObject()));
        }
        if (positionKey != null) {
            document.put(positionKey + ".$", positionValue);
        }
        return document;
    }

    public Fields include(String field) {
        criteria.put(field, 1);
        return this;
    }

    public Fields include(String... fields) {
        for (String key : fields) {
            criteria.put(key, 1);
        }
        return this;
    }

    public Fields exclude(String field) {
        criteria.put(field, 0);
        return this;
    }

    public Fields exclude(String... fields) {
        for (String key : fields) {
            criteria.put(key, 0);
        }
        return this;
    }

    public Fields slice(String field, int size) {
        slices.put(field, size);
        return this;
    }

    public Fields slice(String field, int offset, int size) {
        slices.put(field, Arrays.asList(offset, size));
        return this;
    }

    public Fields elemMatch(String field, Criteria elemMatchCriteria) {
        elemMatchs.put(field, elemMatchCriteria);
        return this;
    }

    public Fields position(String field, int value) {
        positionKey = field;
        positionValue = value;
        return this;
    }


}
