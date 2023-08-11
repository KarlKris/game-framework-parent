package com.echo;

import com.echo.common.convert.core.GenericConversionService;
import com.echo.common.convert.support.DefaultConversionService;
import com.echo.entity.Apple;
import com.echo.mongo.convert.GenericMongoConverter;
import com.echo.mongo.convert.MongoCustomConversions;
import com.echo.mongo.core.EntityOperations;
import com.echo.mongo.core.MongoTemplate;
import com.echo.mongo.core.QueryOperations;
import com.echo.mongo.mapping.MongoManagedTypes;
import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.query.Criteria;
import com.echo.mongo.query.Query;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author: li-yuanwen
 */
public class MongoTests {

    private MongoTemplate prepare() {
        String url = "mongodb://localhost:27017";
        MongoClient mongoClient = MongoClients.create(url);
        String dbName = "Fruit";
        MongoDatabase database = mongoClient.getDatabase(dbName);
        PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry()
                , CodecRegistries.fromProviders(pojoCodecProvider));


        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());

        MongoMappingContext mappingContext = new MongoMappingContext();
        MongoManagedTypes managedTypes = MongoManagedTypes.from(Apple.class);
        mappingContext.setManagedTypes(managedTypes);
        mappingContext.initialize();

        GenericConversionService genericConversionService = new DefaultConversionService();
        GenericMongoConverter mongoConverter = new GenericMongoConverter(genericConversionService, mappingContext);
        mongoConverter.setCodecRegistryProvider(() -> codecRegistry);
        mongoConverter.setCustomConversions(conversions);

        EntityOperations entityOperations = new EntityOperations(mappingContext);

        QueryOperations queryOperations = new QueryOperations(mongoConverter, mappingContext, genericConversionService);

//        POJO数据映射
//        MongoCollection<Apple> collection = database.getCollection("apple", Apple.class)
//                .withCodecRegistry(codecRegistry);
//
//        Apple apple = new Apple();
//        apple.setId(2);
//        apple.setName("a2");
//        apple.setColor(Color.RED);
//        Address address = new Address();
//        address.setName("GuangZhou");
//        apple.setAddress(address);
//        collection.insertOne(apple);

        return new MongoTemplate(database, entityOperations, queryOperations, mongoConverter);
    }


    @Test
    public void queryTest() {
        MongoTemplate mongoTemplate = prepare();
        Criteria criteria = Criteria.where("_id").is(1);
        Query query = new Query(criteria);
        List<Apple> list = mongoTemplate.find(query, Apple.class);
        System.out.println(list.size());
    }


}
