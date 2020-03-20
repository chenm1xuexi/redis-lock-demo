package com.feifei.redislockdemo.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * @author shixiongfei
 * @date 2020-03-19
 * @since
 */
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
@AllArgsConstructor
public class MyRedisLock extends FeifeiLock {

    StringRedisTemplate redisTemplate;

    String key;

    String value;

    @Override
    long unlock() {
        return redisTemplate.execute(
                new DefaultRedisScript<>(COMPARE_AND_DELETE, Long.class),
                Collections.singletonList(key),
                value);
    }
}
