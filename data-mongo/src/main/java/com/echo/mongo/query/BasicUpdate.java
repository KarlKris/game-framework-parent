package com.echo.mongo.query;

import org.bson.Document;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author: li-yuanwen
 */
public class BasicUpdate extends Update {

    private final Document updateObject;

    public BasicUpdate(String updateString) {
        super();
        this.updateObject = Document.parse(updateString);
    }

    public BasicUpdate(Document updateObject) {
        super();
        this.updateObject = updateObject;
    }

    @Override
    public Update set(String key, Object value) {
        updateObject.put("$set", Collections.singletonMap(key, value));
        return this;
    }

    @Override
    public Update unset(String key) {
        updateObject.put("$unset", Collections.singletonMap(key, 1));
        return this;
    }

    @Override
    public Update inc(String key, Number inc) {
        updateObject.put("$inc", Collections.singletonMap(key, inc));
        return this;
    }

    @Override
    public Update push(String key, Object value) {
        updateObject.put("$push", Collections.singletonMap(key, value));
        return this;
    }

    @Override
    public Update addToSet(String key, Object value) {
        updateObject.put("$addToSet", Collections.singletonMap(key, value));
        return this;
    }

    @Override
    public Update pop(String key, Position pos) {
        updateObject.put("$pop", Collections.singletonMap(key, (pos == Position.FIRST ? -1 : 1)));
        return this;
    }

    @Override
    public Update pull(String key, Object value) {
        updateObject.put("$pull", Collections.singletonMap(key, value));
        return this;
    }

    @Override
    public Update pullAll(String key, Object[] values) {
        Document keyValue = new Document();
        keyValue.put(key, Arrays.copyOf(values, values.length));
        updateObject.put("$pullAll", keyValue);
        return this;
    }

    @Override
    public Update rename(String oldName, String newName) {
        updateObject.put("$rename", Collections.singletonMap(oldName, newName));
        return this;
    }

    @Override
    public Document getUpdateObject() {
        return updateObject;
    }

}
