package com.demo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

/**
 * @author mort
 * @Description
 * @date 2021/9/26
 **/
@Slf4j
@Configuration
@PropertySource(value = "classpath:redis.properties")
public class RedisConfig {

    @Value("#{'${redis.cluster.nodes}'.split(',')}")
    private List<String> nodes;
    @Value("${redis.timeout}")
    private Integer timeOut;
    @Value("${redis.maxWaitMillis}")
    private Integer maxWait;
    @Value("${redis.maxTotal}")
    private Integer maxTotal;
    @Value("${redis.maxIdle}")
    private Integer maxIdle;
    @Value("${redis.minIdle}")
    private Integer minIdle;


    @Value("#{'${acc.redis.cluster.nodes}'.split(',')}")
    private List<String> accNodes;
    @Value("${acc.redis.timeout}")
    private Integer accTimeOut;
    @Value("${acc.redis.maxWaitMillis}")
    private Integer accMaxWait;
    @Value("${acc.redis.maxTotal}")
    private Integer accMaxTotal;
    @Value("${acc.redis.maxIdle}")
    private Integer accMaxIdle;
    @Value("${acc.redis.minIdle}")
    private Integer accMinIdle;

    @Bean(name = "redisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory() {

        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(nodes);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxWaitMillis(maxWait);

        LettucePoolingClientConfiguration lettucePoolingClientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeOut))
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(redisClusterConfiguration, lettucePoolingClientConfiguration);
    }

    /**
     * 支持 key是string value是Object的存储 使用 jackson2JsonRedisSerializer 序列化object
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory factory) {
        log.info("=====> redisTemplate start ...");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(mapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "stringStringRedisTemplate")
    public RedisTemplate stringStringRedisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory jedisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }



    @Bean(name = "accRedisConnectionFactory")
    public RedisConnectionFactory accRedisConnectionFactory() {

        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(accNodes);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

        poolConfig.setMinIdle(accMinIdle);
        poolConfig.setMaxIdle(accMaxIdle);
        poolConfig.setMaxTotal(accMaxTotal);
        poolConfig.setMaxWaitMillis(accMaxWait);

        LettucePoolingClientConfiguration lettucePoolingClientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(accTimeOut))
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(redisClusterConfiguration, lettucePoolingClientConfiguration);
    }


    @Bean(name = "accStringStringRedisTemplate")
    public RedisTemplate accStringStringRedisTemplate(@Qualifier("accRedisConnectionFactory") RedisConnectionFactory accRedisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(accRedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }


    @Bean(name = "accStringObjectRedisTemplate")
    public RedisTemplate<String, Object> accStringObjectRedisTemplate(@Qualifier("accRedisConnectionFactory") RedisConnectionFactory factory) {
        log.info("=====> redisTemplate start ...");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(mapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

}
