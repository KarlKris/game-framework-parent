package com.echo.autoconfigure.ramcache;

import com.echo.ioc.anno.Bean;
import com.echo.ioc.anno.ConditionalOnClass;
import com.echo.ioc.anno.Configuration;
import com.echo.mongo.core.MongoTemplate;
import com.echo.ramcache.entity.DataAccessor;
import com.echo.ramcache.entity.DataPersistence;
import com.echo.ramcache.entity.GenericDataPersistence;
import com.echo.ramcache.entity.GenericEntityCacheService;

/**
 * @author: li-yuanwen
 */
@Configuration
@ConditionalOnClass("com.mongodb.client.MongoClient")
public class MongoRamCacheAutoConfiguration {


    @Bean
    public DataAccessor mongoDataAccessor(MongoTemplate mongoTemplate) {
        return new MongoDataAccessor(mongoTemplate);
    }

    @Bean
    public DataPersistence dataPersistence(DataAccessor dataAccessor) {
        return new GenericDataPersistence(dataAccessor);
    }

    @Bean
    public GenericEntityCacheService genericEntityCacheService(DataAccessor dataAccessor, DataPersistence dataPersistence) {
        return new GenericEntityCacheService(dataAccessor, dataPersistence);
    }

    @Bean
    public RamCacheShutdown ramcacheShutdown(DataPersistence dataPersistence) {
        return new RamCacheShutdown(dataPersistence);
    }

}
