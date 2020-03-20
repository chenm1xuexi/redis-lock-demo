package com.feifei.redislockdemo;

import com.feifei.redislockdemo.config.FeifeiRedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * @author shixiongfei
 * @date 2020-03-19
 * @since
 */
@Slf4j
@Component
public class TestCommandRunner implements CommandLineRunner {

    @Autowired
    FeifeiRedisLockUtil redisLock;

    @Autowired
    StringRedisTemplate template;

    @Override
    public void run(String... args) {
        String key = "test";
        // 测试20个线程并发获取锁
        for (int i = 0; i < 20; i++) {
//            new Thread(() -> {
//                String value = UUID.randomUUID().toString();
//                try {
//                    redisLock.tryBlockingLock(key, value, 10, TimeUnit.SECONDS);
//                    // 业务处理
//                    log.info("当前线程获取锁:" + Thread.currentThread().getName() + " value = " + template.opsForValue().get("lock:" + key));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    redisLock.unLock(key, value);
//                }
//            }, "飞飞" + i).start();

            // 锁的过期时间和重试次数已经线程每次睡眠的时间一定要搭配好，不然容易出现获取锁失败,尽量重试次数 * 睡眠时间 接近于锁超时时间
            new Thread(() -> {
                try {
                    String result = redisLock.tryLock(key, () -> "success", 500, TimeUnit.MILLISECONDS, 20, 20);
                    System.out.println("result = " + result);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }, "飞飞" + i).start();
        }
    }
}
