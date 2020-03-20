package com.feifei.redislockdemo.config;

import lombok.extern.slf4j.Slf4j;


/**
 * 实现AutoCloseable 在try{}后无需编写finally，会自动释放
 *
 * @author shixiongfei
 * @date 2020-03-19
 * @since
 */
@Slf4j
public abstract class FeifeiLock implements AutoCloseable {

    /**
     * 锁前缀
     */
    public static final String LOCK_PREFIX = "lock:";

    /**
     * 定义lua脚本，用来进行锁释放
     */
    protected static final String COMPARE_AND_DELETE =
            "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call('del',KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";

    /**
     * 自定义锁释放
     *
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    abstract long unlock();

    @Override
    public void close() {
        long result = this.unlock();
        if (result == 0) {
            log.error(Thread.currentThread().getName() + "锁释放失败");
        }
        log.info(Thread.currentThread().getName() + "释放锁成功");
    }
}
