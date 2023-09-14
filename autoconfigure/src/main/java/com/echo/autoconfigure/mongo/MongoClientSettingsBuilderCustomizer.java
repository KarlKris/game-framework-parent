package com.echo.autoconfigure.mongo;

import com.mongodb.MongoClientSettings;

/**
 * MongoProperties 映射进 MongoClientSettings
 *
 * @author: li-yuanwen
 */
public interface MongoClientSettingsBuilderCustomizer {


    void customize(MongoClientSettings.Builder clientSettingsBuilder);

}
