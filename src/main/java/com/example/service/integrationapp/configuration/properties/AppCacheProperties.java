package com.example.service.integrationapp.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.cache")
public class AppCacheProperties {
    private final List<String> cacheNames = new ArrayList<>();

    private final Map<String, CacheProperties> caches = new HashMap<>();
    private CacheType cacheType;
    @Data
    public static class CacheProperties {
        private Duration expire = Duration.ZERO;

    }

    public interface CacheNames{
        String DATABASE_ENTITIES = "databaseEntities";
        String DATABASE_ENTITIES_BY_ID = "databaseEntityById";
        String DATABASE_ENTITIES_BY_NAME = "databaseEntityByName";
    }

    public enum CacheType {
        IN_MEMORY, REDIS
    }
}
