package com.echo.mongo.query;

import cn.hutool.core.collection.CollectionUtil;
import com.echo.common.util.StringUtils;
import com.echo.mongo.excetion.InvalidMongoDbApiUsageException;
import com.mongodb.BasicDBList;
import org.bson.BsonType;
import org.bson.Document;

import java.util.*;
import java.util.regex.Pattern;

/**
 * mongodb 查询条件
 * @author: li-yuanwen
 */
public class Criteria {

    /** 空值 **/
    private static final Object NOT_SET = new Object();

    /** 查询字段 **/
    private String key;
    /** 条件链表 **/
    private List<Criteria> criteriaChain;
    /** 条件 **/
    private final LinkedHashMap<String, Object> criteria = new LinkedHashMap<>();
    /** 值 **/
    private Object isValue = NOT_SET;

    public Criteria() {
        this.criteriaChain = new ArrayList<>();
    }

    public Criteria(String key) {
        this.key = key;
        this.criteriaChain = new ArrayList<>();
        this.criteriaChain.add(this);
    }

    protected Criteria(List<Criteria> criteriaChain, String key) {
        this.criteriaChain = criteriaChain;
        this.criteriaChain.add(this);
        this.key = key;
    }

    public static Criteria where(String key) {
        return new Criteria(key);
    }

    public Criteria and(String key) {
        return new Criteria(this.criteriaChain, key);
    }

    public Criteria is(Object value) {
        if (!isValue.equals(NOT_SET)) {
            throw new InvalidMongoDbApiUsageException(
                    "Multiple 'is' values declared; You need to use 'and' with multiple criteria");
        }

        if (lastOperatorWasNot()) {
            throw new InvalidMongoDbApiUsageException("Invalid query: 'not' can't be used with 'is' - use 'ne' instead");
        }

        this.isValue = value;
        return this;

    }

    public Criteria isNull() {
        return is(null);
    }

    public Criteria isNullValue() {
        criteria.put("$type", BsonType.NULL.getValue());
        return this;
    }

    /**
     * mongodb 查询关键字$ne
     * @param value 值
     * @return Criteria
     */
    public Criteria ne(Object value) {
        criteria.put("$ne", value);
        return this;
    }

    /**
     * mongodb 查询关键字$lt <(小于)
     * @param value 值
     * @return Criteria
     */
    public Criteria lt(Object value) {
        criteria.put("$lt", value);
        return this;
    }

    /**
     * mongodb 查询关键字$lte <=(小于等于)
     * @param value 值
     * @return Criteria
     */
    public Criteria lte(Object value) {
        criteria.put("$lte", value);
        return this;
    }

    /**
     * mongodb 查询关键字$gt >(大于)
     * @param value 值
     * @return Criteria
     */
    public Criteria gt(Object value) {
        criteria.put("$gt", value);
        return this;
    }

    /**
     * mongodb 查询关键字$gte >=(大于等于)
     * @param value 值
     * @return Criteria
     */
    public Criteria gte(Object value) {
        criteria.put("$gte", value);
        return this;
    }

    /**
     * mongodb 查询关键字$in
     * @param values 值
     * @return Criteria
     */
    public Criteria in(Object... values) {
        if (values.length > 1 && values[1] instanceof Collection) {
            throw new InvalidMongoDbApiUsageException(
                    "You can only pass in one argument of type " + values[1].getClass().getName());
        }
        criteria.put("$in", Arrays.asList(values));
        return this;
    }

    /**
     * mongodb 查询关键字$in
     * @param values 值
     * @return Criteria
     */
    public Criteria in(Collection<?> values) {
        criteria.put("$in", values);
        return this;
    }

    /**
     * mongodb 查询关键字$nin
     * @param values 值
     * @return Criteria
     */
    public Criteria nin(Object... values) {
        return nin(Arrays.asList(values));
    }

    /**
     * mongodb 查询关键字$nin
     * @param values 值
     * @return Criteria
     */
    public Criteria nin(Collection<?> values) {
        criteria.put("$nin", values);
        return this;
    }

    /**
     * mongodb 关键字$mod
     * @param value
     * @param remainder
     * @return
     */
    public Criteria mod(Number value, Number remainder) {
        List<Object> l = new ArrayList<>(2);
        l.add(value);
        l.add(remainder);
        criteria.put("$mod", l);
        return this;
    }

    /**
     * mongodb 查询关键字$all
     * @param values 值
     * @return Criteria
     */
    public Criteria all(Collection<?> values) {
        criteria.put("$all", values);
        return this;
    }

    /**
     * mongodb 查询关键字$all
     * @param values 值
     * @return Criteria
     */
    public Criteria all(Object... values) {
        return all(Arrays.asList(values));
    }

    /**
     * mongodb 查询关键字$size
     * @param size 值
     * @return Criteria
     */
    public Criteria size(int size) {
        criteria.put("$size", size);
        return this;
    }

    /**
     * mongodb 查询关键字$exists
     * @param value 值
     * @return Criteria
     */
    public Criteria exists(boolean value) {
        criteria.put("$exists", value);
        return this;
    }

    /**
     * mongodb 查询关键字$type 基于BSON类型来检索集合中匹配的数据类型，并返回结果。
     * @param typeNumber 值
     * @return Criteria
     */
    public Criteria type(int typeNumber) {
        criteria.put("$type", typeNumber);
        return this;
    }

    /**
     * mongodb 查询关键字$not
     * @param value 值
     * @return Criteria
     */
    private Criteria not(Object value) {
        criteria.put("$not", value);
        return this;
    }

    /**
     * mongodb 查询关键字$regex
     * @param pattern 正则表达式
     * @return Criteria
     */
    public Criteria regex(Pattern pattern) {
        if (lastOperatorWasNot()) {
            return not(pattern);
        }

        this.isValue = pattern;
        return this;
    }

    /**
     * mongodb 查询关键字$or
     * @param criteria Criteria
     * @return Criteria
     */
    public Criteria orOperator(Collection<Criteria> criteria) {
        BasicDBList bsonList = createCriteriaList(criteria);
        return registerCriteriaChainElement(new Criteria("$or").is(bsonList));
    }

    /**
     * mongodb 查询关键字$or
     * @param criteria Criteria
     * @return Criteria
     */
    public Criteria orOperator(Criteria... criteria) {
        return orOperator(Arrays.asList(criteria));
    }

    /**
     * mongodb 查询关键字$nor
     * @param criteria Criteria
     * @return Criteria
     */
    public Criteria norOperator(Collection<Criteria> criteria) {
        BasicDBList bsonList = createCriteriaList(criteria);
        return registerCriteriaChainElement(new Criteria("$nor").is(bsonList));
    }

    /**
     * mongodb 查询关键字$nor
     * @param criteria Criteria
     * @return Criteria
     */
    public Criteria norOperator(Criteria... criteria) {
        return norOperator(Arrays.asList(criteria));
    }

    /**
     * mongodb 查询关键字$and
     * @param criteria Criteria
     * @return Criteria
     */
    public Criteria andOperator(Collection<Criteria> criteria) {
        BasicDBList bsonList = createCriteriaList(criteria);
        return registerCriteriaChainElement(new Criteria("$and").is(bsonList));
    }

    /**
     * mongodb 查询关键字$and
     * @param criteria Criteria
     * @return Criteria
     */
    public Criteria andOperator(Criteria... criteria) {
        return andOperator(Arrays.asList(criteria));
    }

    private boolean lastOperatorWasNot() {
        return !this.criteria.isEmpty() && "$not".equals(this.criteria.keySet().toArray()[this.criteria.size() - 1]);
    }

    private Criteria registerCriteriaChainElement(Criteria criteria) {

        if (lastOperatorWasNot()) {
            throw new IllegalArgumentException(
                    "operator $not is not allowed around criteria chain element: " + criteria.getCriteriaObject());
        } else {
            criteriaChain.add(criteria);
        }
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public Document getCriteriaObject() {

        if (this.criteriaChain.size() == 1) {
            return criteriaChain.get(0).getSingleCriteriaObject();
        } else if (CollectionUtil.isEmpty(this.criteriaChain) && !CollectionUtil.isEmpty(this.criteria)) {
            return getSingleCriteriaObject();
        } else {
            Document criteriaObject = new Document();
            for (Criteria c : this.criteriaChain) {
                Document document = c.getSingleCriteriaObject();
                for (String k : document.keySet()) {
                    setValue(criteriaObject, k, document.get(k));
                }
            }
            return criteriaObject;
        }
    }

    protected Document getSingleCriteriaObject() {
        Document document = new Document();
        boolean not = false;
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            if (not) {
                Document notDocument = new Document();
                notDocument.put(key, value);
                document.put("$not", notDocument);
                not = false;
            } else {
                if ("$not".equals(key) && value == null) {
                    not = true;
                } else {
                    document.put(key, value);
                }
            }
        }
        if (!StringUtils.hasLength(this.key)) {
            if (not) {
                return new Document("$not", document);
            }
            return document;
        }
        Document queryCriteria = new Document();
        if (!NOT_SET.equals(isValue)) {
            queryCriteria.put(this.key, this.isValue);
            queryCriteria.putAll(document);
        } else {
            queryCriteria.put(this.key, document);
        }

        return queryCriteria;
    }

    private void setValue(Document document, String key, Object value) {
        Object existing = document.get(key);
        if (existing == null) {
            document.put(key, value);
        } else {
            throw new InvalidMongoDbApiUsageException("Due to limitations of the org.bson.Document, "
                    + "you can't add a second '" + key + "' expression specified as '" + key + " : " + value + "';"
                    + " Criteria already contains '" + key + " : " + existing + "'");
        }
    }

    private BasicDBList createCriteriaList(Collection<Criteria> criteria) {
        BasicDBList bsonList = new BasicDBList();
        for (Criteria c : criteria) {
            bsonList.add(c.getCriteriaObject());
        }
        return bsonList;
    }

}
