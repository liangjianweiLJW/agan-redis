package com.agan.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @Description: redission读写锁测试
 * @Author: jianweil
 * @date: 2022/1/27 20:20
 */
@Slf4j
public class RReadWriteLockTest {
    private static final String KEY_LOCKED = "myLock";
    private static RedissonClient redissonClient = null;

    public static void main(String[] args) {
        initRedissonClient();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    readWrite();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread" + i).start();
        }
    }

    private static void initRedissonClient() {
        // 1. Create config object
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 2. Create Redisson instance
        RReadWriteLockTest.redissonClient = Redisson.create(config);
    }

    /**
     * 读写锁
     */
    private static void readWrite() throws InterruptedException {

        log.info(Thread.currentThread().getName() + " \t 运行");
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(KEY_LOCKED);

        readWriteLock.readLock().lock();
        log.info(Thread.currentThread().getName() + " \t 获取读锁");
        // 模拟处理逻辑用时5s
        Thread.sleep(5 * 1000);
        //释放读锁
        readWriteLock.readLock().unlock();
        log.info(Thread.currentThread().getName() + " \t 释放读锁");

        readWriteLock.writeLock().lock();
        log.info(Thread.currentThread().getName() + " \t 获取写锁");
        // 模拟处理逻辑用时5s
        Thread.sleep(5 * 1000);
        //释放读写
        readWriteLock.writeLock().unlock();
        log.info(Thread.currentThread().getName() + " \t 释放写锁");
    }

}
