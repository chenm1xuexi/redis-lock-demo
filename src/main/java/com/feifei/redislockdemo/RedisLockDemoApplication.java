package com.feifei.redislockdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

@Slf4j
@SpringBootApplication
public class RedisLockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisLockDemoApplication.class, args);
    }

}
