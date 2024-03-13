package com.example.service.integrationapp.configuration;
import com.example.service.integrationapp.configuration.properties.AppCacheProperties;
import com.google.common.cache.CacheBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(AppCacheProperties.class)

public class CacheConfiguration {

//    @Bean
//    @ConditionalOnExpression("'${app.cache.cacheType}'.equals('inMemory')")
//    public ConcurrentMapCacheManager inMemoryCacheManager(AppCacheProperties appCacheProperties){
//        var cacheManager = new ConcurrentMapCacheManager() {
//            @Override
//            protected Cache createConcurrentMapCache(String name) {
//                return new ConcurrentMapCache(
//                        name,
//                        CacheBuilder.newBuilder()
//                                .expireAfterWrite(appCacheProperties.getCaches().get(name).getExpire())
//                                .build()
//                                .asMap(),
//                        true
//                );
//            }
//        };
//
//        var cacheNames = appCacheProperties.getCacheNames();
//        if (!cacheNames.isEmpty()){
//            cacheManager.setCacheNames(cacheNames);
//        }
//
//        return cacheManager;
//    }
//    @Bean
//    @ConditionalOnExpression("'${app.cache.cacheType}'.equals('inMemory')")
//    public ConcurrentMapCacheManager inMemoryCacheManager(AppCacheProperties appCacheProperties){
//        var cacheManager = new ConcurrentMapCacheManager() {
//            @Override
//            protected Cache createConcurrentMapCache(String name) {
//                var cacheProperties = appCacheProperties.getCaches().get(name);
//                if (cacheProperties != null) {
//                    return new ConcurrentMapCache(
//                            name,
//                            CacheBuilder.newBuilder()
//                                    .expireAfterWrite(cacheProperties.getExpire())
//                                    .build()
//                                    .asMap(),
//                            true
//                    );
//                }
//                return super.createConcurrentMapCache(name);
//            }
//        };
//
//        var cacheNames = appCacheProperties.getCacheNames();
//        if (!cacheNames.isEmpty()){
//            cacheManager.setCacheNames(cacheNames);
//        }
//
//        return cacheManager;
//    }

    @Bean
    @ConditionalOnProperty(prefix = "app.redis", name = "enable", havingValue = "true")
    @ConditionalOnExpression("'${app.cache.cacheType}'.equals('redis')")
    public CacheManager redisCacheManager(AppCacheProperties appCacheProperties, LettuceConnectionFactory lettuceConnectionFactory){
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig();
        Map<String, RedisCacheConfiguration> redisCacheConfiguration = new HashMap<>();

        appCacheProperties.getCacheNames().forEach(cacheName -> {
            redisCacheConfiguration.put(cacheName, RedisCacheConfiguration.defaultCacheConfig().entryTtl(
                    appCacheProperties.getCaches().get(cacheName).getExpire()
            ));
        });

        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(redisCacheConfiguration)
                .build();

    }


}
