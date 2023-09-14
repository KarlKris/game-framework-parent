package com.echo.autoconfigure.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;

import java.util.function.BiFunction;

/**
 * @author: li-yuanwen
 */
public abstract class MongoClientFactorySupport<T> {

    private final MongoClientSettingsBuilderCustomizer builderCustomizer;

    private final BiFunction<MongoClientSettings, MongoDriverInformation, T> clientCreator;

    protected MongoClientFactorySupport(MongoClientSettingsBuilderCustomizer builderCustomizer,
                                        BiFunction<MongoClientSettings, MongoDriverInformation, T> clientCreator) {
        this.builderCustomizer = builderCustomizer;
        this.clientCreator = clientCreator;
    }

    public T createMongoClient(MongoClientSettings settings) {
        MongoClientSettings.Builder targetSettings = MongoClientSettings.builder(settings);
        customize(targetSettings);
        return this.clientCreator.apply(targetSettings.build(), driverInformation());
    }

    private void customize(MongoClientSettings.Builder builder) {
        builderCustomizer.customize(builder);
    }

    private MongoDriverInformation driverInformation() {
        return MongoDriverInformation.builder(MongoDriverInformation.builder().build()).driverName("game-mongo")
                .build();
    }

}
