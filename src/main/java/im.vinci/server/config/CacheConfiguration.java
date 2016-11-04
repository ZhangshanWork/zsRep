package im.vinci.server.config;

import com.google.common.cache.CacheBuilder;
import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;
import im.vinci.server.utils.cache.Cache;
import im.vinci.server.utils.cache.GuavaCache;
import im.vinci.server.utils.cache.MemcachedCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Created by henryhome on 3/19/15.
 */
@Configuration
public class CacheConfiguration {

    @Autowired
    Environment env;

    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource(value = {"classpath:/intg/cache.properties"})
    static class ServiceIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource(value = {"classpath:/qaci/cache.properties"})
    static class ServiceQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource(value = {"classpath:/prod/cache.properties"})
    static class ServiceProdAConfiguration {
    }

    @Bean(destroyMethod = "shutDown")
    @ConditionalOnProperty(value = "spring.cache.type",havingValue="memcached")
    public SockIOPool getMemcachedSockPool() {
        SockIOPool pool = SockIOPool.getInstance();
        System.err.println("0-0----:"+ Arrays.toString(env.getProperty("spring.cache.cache-names").split(",")));
        pool.setServers(env.getProperty("spring.cache.cache-names").split(","));
        pool.initialize();
        return pool;
    }

    public MemCachedClient getMemcachedClient() {
        return new MemCachedClient();
    }

    @Bean
    public Cache cacheManager() {
        String type = env.getProperty("spring.cache.type");
        if (type == null) {
            type = "guava";
        }
        switch (type) {
            case "memcached":
                return new MemcachedCache(getMemcachedClient());
            case "guava":
            default:
                return new GuavaCache("cache", CacheBuilder.newBuilder().maximumSize(100).build());
        }
    }

//    @Bean
//    public JedisConnectionFactory jedisConnectionFactory() {
//        JedisConnectionFactory factory = new JedisConnectionFactory();
//        factory.setHostName(env.getProperty("cache.host"));
//        factory.setUsePool(true);
//        return factory;
//    }
//
//    @Bean
//    public StringRedisSerializer stringRedisSerializer() {
//        return new StringRedisSerializer();
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(jedisConnectionFactory());
//        redisTemplate.setKeySerializer(stringRedisSerializer());
//        redisTemplate.setHashKeySerializer(stringRedisSerializer());
//        return redisTemplate;
//    }
//
//    @Bean
//    public CacheManager cacheManager() {
//        return new RedisCacheManager(redisTemplate());
//    }
}