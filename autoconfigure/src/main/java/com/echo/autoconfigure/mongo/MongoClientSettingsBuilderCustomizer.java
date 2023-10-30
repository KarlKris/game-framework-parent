package com.echo.autoconfigure.mongo;

import com.echo.autoconfigure.SettingsBuilderCustomizer;
import com.mongodb.MongoClientSettings;

/**
 * MongoProperties 映射进 MongoClientSettings
 *
 * @author: li-yuanwen
 */
public interface MongoClientSettingsBuilderCustomizer extends SettingsBuilderCustomizer<MongoClientSettings.Builder> {


}
