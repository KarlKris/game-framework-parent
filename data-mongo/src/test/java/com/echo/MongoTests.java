package com.echo;

import com.echo.common.convert.core.GenericConversionService;
import com.echo.common.convert.support.DefaultConversionService;
import com.echo.entity.Apple;
import com.echo.model.*;
import com.echo.mongo.MongoDatabaseFactory;
import com.echo.mongo.SimpleMongoDatabaseFactory;
import com.echo.mongo.convert.GenericMongoConverter;
import com.echo.mongo.convert.MongoCustomConversions;
import com.echo.mongo.core.MongoTemplate;
import com.echo.mongo.mapping.MongoManagedTypes;
import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.query.Criteria;
import com.echo.mongo.query.Query;
import com.echo.mongo.query.Update;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.junit.Test;

import java.util.*;

/**
 * @author: li-yuanwen
 */
public class MongoTests {

    private MongoTemplate prepare() {
        String url = "mongodb://localhost:27017";
        MongoClient mongoClient = MongoClients.create(url);
        String dbName = "Fruit";
        MongoDatabaseFactory mongoDatabaseFactory = new SimpleMongoDatabaseFactory(mongoClient, dbName);

        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());

        MongoMappingContext mappingContext = new MongoMappingContext();
        MongoManagedTypes managedTypes = MongoManagedTypes.from(Apple.class);
        mappingContext.setManagedTypes(managedTypes);
        mappingContext.initialize();
        mappingContext.setAutoIndexCreation(true);

        GenericConversionService genericConversionService = new DefaultConversionService();
        GenericMongoConverter mongoConverter = new GenericMongoConverter(genericConversionService, mappingContext);
        mongoConverter.setCustomConversions(conversions);
        return new MongoTemplate(mongoDatabaseFactory, mongoConverter);
    }


    @Test
    public void queryTest() {
        MongoTemplate mongoTemplate = prepare();
        Criteria criteria = Criteria.where("_id").lte(8);
        Query query = new Query(criteria);
        List<Apple> list = mongoTemplate.find(query, Apple.class);
        System.out.println(list.size());
    }

    @Test
    public void findOneTest() {
        MongoTemplate mongoTemplate = prepare();
        Criteria criteria = Criteria.where("_id").gt(15);
        Query query = new Query(criteria);
        Apple apple = mongoTemplate.findOne(query, Apple.class);
        System.out.println(apple.getAddress().getName());
    }

    @Test
    public void findByIdTest() {
        MongoTemplate mongoTemplate = prepare();
        Apple apple = mongoTemplate.findById(17, Apple.class);
        System.out.println(apple.getAddress().getName());
    }


    @Test
    public void insertTest() {
        MongoTemplate mongoTemplate = prepare();

        Apple apple = new Apple();
        apple.setId(28);
        apple.setMonth(10);
        apple.setName("r12");
        apple.setColor(Color.GREEN);
        Address address = new Address();
        address.setName("Xianyang");
        apple.setAddress(address);

        Map<Integer, Info> map = new HashMap<>(2);
        A a = new A();
        a.setId("a");
        map.put(1, a);
        B b = new B();
        b.setId(2);
        map.put(2, b);
        apple.setMap(map);

        List<Model> list = new ArrayList<>(1);
        Model model = new Model();
        model.setId(5);
        list.add(model);
        apple.setList(list);

        mongoTemplate.insert(apple);
    }

    @Test
    public void removeTest() {
        MongoTemplate mongoTemplate = prepare();

        Criteria criteria = Criteria.where("_id").is(15);
        Query query = new Query(criteria);
        DeleteResult result = mongoTemplate.remove(query, Apple.class);
        System.out.println(result.getDeletedCount());
    }


    @Test
    public void updateTest() {
        MongoTemplate mongoTemplate = prepare();
        Criteria criteria = Criteria.where("_id").is(16);
        Query query = new Query(criteria);

        Update update = Update.update("n", "z16");
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Apple.class);
        System.out.println(updateResult.getMatchedCount());
    }

    @Test
    public void saveTest() {
        MongoTemplate mongoTemplate = prepare();

        Apple apple = new Apple();
        apple.setId(18);
        apple.setName("r13");
        apple.setColor(Color.GREEN);
        Address address = new Address();
        address.setName("Xianyang");
        apple.setAddress(address);

        Map<Integer, Info> map = new HashMap<>(2);
        A a = new A();
        a.setId("a");
        map.put(1, a);
        B b = new B();
        b.setId(2);
        map.put(2, b);
        apple.setMap(map);

        List<Model> list = new ArrayList<>(1);
        Model model = new Model();
        model.setId(5);
        list.add(model);
        apple.setList(list);

        mongoTemplate.save(apple);
    }

}
