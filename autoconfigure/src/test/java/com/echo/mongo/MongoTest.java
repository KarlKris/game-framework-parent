package com.echo.mongo;

import com.echo.ioc.context.AnnotationGenericApplicationContext;
import com.echo.mongo.core.MongoTemplate;
import com.echo.mongo.entity.Apple;
import com.echo.mongo.query.Criteria;
import com.echo.mongo.query.Query;
import org.junit.Test;

import java.util.List;

/**
 * @author: li-yuanwen
 */
public class MongoTest {

    @Test
    public void mongoTest() {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        MongoTemplate mongoTemplate = context.getBean(MongoTemplate.class);
        Criteria criteria = Criteria.where("_id").lte(8);
        Query query = new Query(criteria);
        List<Apple> list = mongoTemplate.find(query, Apple.class);
        System.out.println(list.size());

    }

}
