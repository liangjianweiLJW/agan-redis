package com.agan.redis;

import org.redisson.Redisson;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Description: 读写锁
 * @Author: jianweil
 * @date: 2022/1/27 20:04
 */
public class ReadWriteLockTest2 {
    private static final String KEY_LOCKED = "myLock";
    private static RedissonClient redissonClient = null;

    private static void initRedissonClient() {
        // 1. Create config object
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 2. Create Redisson instance
        ReadWriteLockTest2.redissonClient = Redisson.create(config);
    }

    public static void read() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(KEY_LOCKED);
        readWriteLock.readLock().lock();
        try {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "获取读锁，开始执行");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "释放读锁");
            readWriteLock.readLock().unlock();
        }
    }

    public static void write() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(KEY_LOCKED);
        readWriteLock.writeLock().lock();
        try {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "获取写锁，开始执行");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "释放写锁");
            readWriteLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) {
        initRedissonClient();
        new Thread(() -> read(), "Thread1").start();
        new Thread(() -> read(), "Thread2").start();
        new Thread(() -> write(), "Thread3").start();
        new Thread(() -> write(), "Thread4").start();
    }
}
