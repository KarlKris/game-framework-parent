package com.echo.autoconfigure.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;


/**
 * @author: li-yuanwen
 */
public class MongoClientFactory extends MongoClientFactorySupport<MongoClient> {

    protected MongoClientFactory(MongoClientSettingsBuilderCustomizer builderCustomizer) {
        super(builderCustomizer, MongoClients::create);
    }
}
