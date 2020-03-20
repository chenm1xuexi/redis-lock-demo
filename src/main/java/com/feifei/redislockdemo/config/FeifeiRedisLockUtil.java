package com.feifei.redislockdemo.config;

import com.feifei.redislockdemo.handler.LockHandler;
import com.feifei.redislockdemo.utils.IdWorker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.feifei.redislockdemo.config.FeifeiLock.LOCK_PREFIX;

/**
 * 自定义redis分布式锁
 *
 * @author shixiongfei
 * @date 2020-03-19
 * @since
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class FeifeiRedisLockUtil {

    StringRedisTemplate redisTemplate;

    /**
     * 定义线程睡眠时间 根据一个接口的响应时间不可超过200ms来定义
     */
    private static final int SLEEP_TIME = 10;

    /**
     * 定义lua脚本，用来进行锁释放
     */
    private static final String COMPARE_AND_DELETE =
            "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call('del',KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";


    /**
     * 阻塞锁，在指定时间内一直获取锁，超过重试次数即放弃
     * key value都交由用户自定义
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     */
    public boolean tryLock(String key, String value, long timeout, TimeUnit timeUnit, int tryTimes) throws InterruptedException {
        // 在阻塞时间内自旋获取锁
        while (tryTimes > 0) {
            if (redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + key, value, timeout, timeUnit)) {
                return true;
            }
            tryTimes--;
            //防止一直消耗 CPU 睡10毫秒
            TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
        }

        return false;
    }

    /**
     * 阻塞锁，在指定时间内一直获取锁，超时则放弃
     * key value都交由用户自定义
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     */
    public boolean tryLock(String key, String value, long blockTime, long timeout, TimeUnit timeUnit) throws InterruptedException {
        // 在阻塞时间内自旋获取锁
        while (blockTime > 0) {
            if (redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + key, value, timeout, timeUnit)) {
                return true;
            }
            blockTime -= SLEEP_TIME;
            //防止一直消耗 CPU 睡10毫秒
            TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
        }

        return false;
    }

    /**
     * 获取阻塞锁, 会一直阻塞，直到获取锁
     * <p>
     * key value都交由用户自定义
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     */
    public boolean tryBlockingLock(String key, String value, long timeout, TimeUnit timeUnit) throws InterruptedException {
        // 一直自旋，直到获取锁
        for (; ; ) {
            if (redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + key, value, timeout, timeUnit)) {
                return true;
            }
            //防止一直消耗 CPU 睡10毫秒
            TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
        }

    }

    /**
     * 获取阻塞锁, 会一直阻塞，直到获取锁
     * <p>
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     */
    public String tryBlockingLock(String key, long timeout, TimeUnit timeUnit) throws InterruptedException {
        String value = UUID.randomUUID().toString();
        // 一直自旋，直到获取锁
        for (; ; ) {
            boolean isLock = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + key, value, timeout, timeUnit);
            if (isLock) {
                return value;
            }
            //防止一直消耗 CPU 睡10毫秒
            TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
        }

    }

    /**
     * 释放锁
     * 采用lua脚本进行锁的释放
     * <p>
     * 获取到锁后，返回value，用于后续的锁释放
     * 存在一个线程获取锁后，执行时间超过了持有时间，此时另外一个线程获取锁，在短时间内释放了锁，而释放的锁是上一个线程的锁
     * 为了屏蔽这种情况，每次释放时都要确认下是否为当前持有锁的线程的释放
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     */
    public void unLock(String key, String value) {
        long result = redisTemplate.execute(new DefaultRedisScript<>(COMPARE_AND_DELETE, Long.class),
                Collections.singletonList(LOCK_PREFIX + key), value);
        if (result == 0) {
            log.error(Thread.currentThread().getName() + "线程下的redis锁释放失败");
        }
        log.info("释放锁：" + result);
    }


    /**
     * 获取锁
     *
     * @param key         redis锁
     * @param timeout     锁超时时间
     * @param retries     重试获取锁次数
     * @return
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     */
    public FeifeiLock acquire(String key, long timeout, TimeUnit timeUnit, int retries, long sleepTime) throws InterruptedException {
        final String value = IdWorker.getId();

        while (retries > 0) {
            // 线程异常中断时，退出自旋
            if (Thread.interrupted()) {
                break;
            }

            if (redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + key, value, timeout, timeUnit)) {
                return new MyRedisLock(redisTemplate, LOCK_PREFIX + key, value);
            }

            // 睡眠指定毫秒数
            TimeUnit.MILLISECONDS.sleep(sleepTime);
            retries--;
        }

        return null;
    }

    /**
     * 获取锁，并进行相应的业务处理
     *
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public <T> T tryLock(String key, LockHandler<T> handler, long timeout, TimeUnit timeUnit, int retries, long sleepTime) throws Throwable {
        // 处理完成后会自动释放锁
        try (FeifeiLock lock = this.acquire(key, timeout, timeUnit, retries, sleepTime)) {
            if (Objects.nonNull(lock)) {
                log.info("get lock success, key: {}", key);
                return handler.handle();
            }
        }
        log.info("get lock fail, key: {}", key);
        return null;
    }
}
