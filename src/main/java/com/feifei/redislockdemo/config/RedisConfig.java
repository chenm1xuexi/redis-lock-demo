package com.feifei.redislockdemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis相关配置
 *
 * @author shixiongfei
 * @date 2020-03-19
 * @since
 */
@Configuration
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializer<?> serializer = new StringRedisSerializer();
        StringRedisTemplate template = new StringRedisTemplate();
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(serializer);
        template.setConnectionFactory(connectionFactory);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
