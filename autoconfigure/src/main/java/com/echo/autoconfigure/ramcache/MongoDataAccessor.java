package com.echo.autoconfigure.ramcache;

import com.echo.mongo.core.MongoTemplate;
import com.echo.mongo.query.Criteria;
import com.echo.mongo.query.Query;
import com.echo.ramcache.entity.AbstractEntity;
import com.echo.ramcache.entity.DataAccessor;
import com.echo.ramcache.entity.IEntity;

import java.io.Serializable;
import java.util.Collection;

/**
 * 基于mongodb的数据访问
 *
 * @author: li-yuanwen
 */
public class MongoDataAccessor implements DataAccessor {

    private final MongoTemplate mongoTemplate;

    public MongoDataAccessor(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public <PK extends Comparable<PK> & Serializable, T extends IEntity<PK>> T load(PK id, Class<T> tClass) {
        return mongoTemplate.findById(id, tClass);
    }

    @Override
    public void remove(AbstractEntity<?> entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public void update(AbstractEntity<?> entity) {
        mongoTemplate.save(entity);
    }

    @Override
    public void create(AbstractEntity<?> entity) {
        mongoTemplate.insert(entity);
    }

    @Override
    public <FK extends Comparable<FK> & Serializable, T extends IEntity<?>> Collection<T> list(FK owner, Class<T> tClass) {
        Criteria criteria = Criteria.where("owner").is(owner);
        return mongoTemplate.find(Query.query(criteria), tClass);
    }
}
