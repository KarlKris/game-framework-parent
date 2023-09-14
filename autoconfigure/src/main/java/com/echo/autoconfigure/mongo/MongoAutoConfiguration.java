package com.echo.autoconfigure.mongo;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import com.echo.common.convert.core.ConfigurableConversionService;
import com.echo.common.convert.support.DefaultConversionService;
import com.echo.ioc.anno.*;
import com.echo.mongo.MongoDatabaseFactory;
import com.echo.mongo.SimpleMongoDatabaseFactory;
import com.echo.mongo.convert.GenericMongoConverter;
import com.echo.mongo.convert.MongoCustomConversions;
import com.echo.mongo.core.MongoTemplate;
import com.echo.mongo.mapping.MongoManagedTypes;
import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.mapping.anno.Document;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;

import java.util.Collections;
import java.util.stream.Stream;

/**
 * 模块 data-mongo 自动注入配置
 *
 * @author: li-yuanwen
 */
@Configuration
@ConditionalOnClass("com.mongodb.client.MongoClient")
@EnableConfigurationProperties(MongoProperties.class)
public class MongoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean("com.echo.common.convert.core.ConfigurableConversionService")
    public ConfigurableConversionService conversionService() {
        return new DefaultConversionService();
    }


    @Bean
    public MongoClient mongoClient(MongoProperties mongoProperties) {
        return new MongoClientFactory(new MongoPropertiesClientSettingsBuilderCustomizer(mongoProperties))
                .createMongoClient(MongoClientSettings.builder().build());
    }


    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, MongoProperties mongoProperties) {
        return new SimpleMongoDatabaseFactory(mongoClient, mongoProperties.getMongoClientDatabase());
    }

    @Bean
    @ConditionalOnMissingBean("com.echo.mongo.convert.MongoCustomConversions")
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Collections.emptyList());
    }

    @Bean
    public MongoMappingContext mongoMappingContext(MongoProperties properties, MongoCustomConversions conversions) {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        mappingContext.setAutoIndexCreation(properties.getAutoIndexCreation());
        Stream<Class<?>> entityStream = ClassScanner.scanAllPackage(properties.getEntityPackage()
                , clazz -> AnnotationUtil.hasAnnotation(clazz, Document.class)).stream();
        mappingContext.setManagedTypes(MongoManagedTypes.fromStream(entityStream));
        mappingContext.initialize();
        return mappingContext;
    }

    @Bean
    public GenericMongoConverter mongoConverter(ConfigurableConversionService conversionService, MongoMappingContext mappingContext
            , MongoCustomConversions conversions) {
        GenericMongoConverter mongoConverter = new GenericMongoConverter(conversionService, mappingContext);
        mongoConverter.setCustomConversions(conversions);
        return mongoConverter;
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, GenericMongoConverter mongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mongoConverter);
    }

}
