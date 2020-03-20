package com.feifei.redislockdemo.handler;

/**
 * @author shixiongfei
 * @date 2020-03-19
 * @since
 */
@FunctionalInterface
public interface LockHandler<T> {

    /**
     * 业务逻辑处理
     *
     * @author shixiongfei
     * @date 2020-03-19
     * @updateDate 2020-03-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    T handle() throws Throwable;


}
