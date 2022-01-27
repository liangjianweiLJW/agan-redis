package com.agan.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

/**
 * @Description: todo
 * @Author: jianweil
 * @date: 2022/1/27 17:33
 */
@Slf4j
public class ClientTest {
    private static final Long TIME_LOCKED = 50 * 1000l;
    private static final String KEY_LOCKED = "myLock";
    private static RedissonClient redissonClient = null;

    public static void main(String[] args) {
        initRedissonClient();
        //lock();
        //lock1();
        getLockmain();
    }

    private static void initRedissonClient() {
        // 1. Create config object
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 2. Create Redisson instance
        ClientTest.redissonClient = Redisson.create(config);
    }

    private static void lock() {
        RLock lock1 = redissonClient.getLock(KEY_LOCKED);
        log.error("lock1 clas: {}", lock1.getClass());
        lock1.lock();
        log.info("lock, ThreadName: {} id: {} locked, 重入次数: {}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());

        // 处理业务逻辑
        try {
            Thread.sleep(TIME_LOCKED);
            reLock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            log.info("lock, ThreadName: {} id: {} unlock, 重入次数: {}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());
        }
    }

    /**
     * 测试锁的重入
     */
    private static void reLock() {
        RLock lock1 = redissonClient.getLock(KEY_LOCKED);
        lock1.lock();
        log.info("reLock, ThreadName: {} id: {} locked, 重入次数: {}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());

        // 处理业务逻辑
        try {
            Thread.sleep(TIME_LOCKED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            log.info("reLock, ThreadName: {} id: {} unlock, 重入次数: {}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());
        }
    }

    /**
     * 也可以使用lock(long var1, TimeUnit var3); 方法自动释放锁
     */
    private static void lock3() {
        RLock lock1 = redissonClient.getLock(KEY_LOCKED);
        log.error("lock1 clas: {}", lock1.getClass());
        // 500s 后自动释放锁
        lock1.lock(500, TimeUnit.SECONDS);
        try {
            Thread.sleep(TIME_LOCKED);
        } catch (InterruptedException ignore) {
            // ignore
        }
    }

    /**
     * tryLock(long time, TimeUnit unit)  可以尝试一定时间去获取锁，返回Boolean值
     */
    private static void lock2() {
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info(Thread.currentThread().getName() + " \t 运行");
                    RLock lock1 = redissonClient.getLock(KEY_LOCKED);
                    try {
                        // 尝试获取锁60s
                        boolean b = lock1.tryLock(7, TimeUnit.SECONDS);
                        if (!b) {
                            log.info(Thread.currentThread().getName() + " \t 获取锁失败");
                            return;
                        }
                    } catch (InterruptedException e) {
                    }

                    log.info(Thread.currentThread().getName() + " \t 获取锁");

                    try {
                        // 模拟处理逻辑用时50s
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {

                    }

                    lock1.unlock();
                    log.info(Thread.currentThread().getName() + " \t 释放锁");
                }
            }).start();
        }
    }

    private static void getLockmain() {
        for (int i = 0; i < 3; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getLock("hhh", 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void getLock(String key, int n) throws InterruptedException {
        //模拟递归，3次递归后退出
        if (n > 3) {
            return;
        }
        //步骤1：获取一个分布式可重入锁RLock
        //分布式可重入锁RLock :实现了java.util.concurrent.locks.Lock接口，同时还支持自动过期解锁。
        RLock lock = redissonClient.getFairLock(key);
        //步骤2：尝试拿锁
        // 1. 默认的拿锁
        //lock.tryLock();
        // 2. 支持过期解锁功能,10秒钟以后过期自动解锁, 无需调用unlock方法手动解锁
        //lock.tryLock(10, TimeUnit.SECONDS);
        // 3. 尝试加锁，最多等待3秒，上锁以后10秒后过期自动解锁
        // lock.tryLock(3, 10, TimeUnit.SECONDS);
        boolean bs = lock.tryLock(4, 10, TimeUnit.SECONDS);
        if (bs) {
            try {
                // 业务代码
                log.info("线程{}业务逻辑处理: {},递归{}", Thread.currentThread().getName(), key, n);
                //模拟处理业务
                Thread.sleep(1000 * 5);
                //模拟进入递归
                getLock(key, ++n);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            } finally {
                //步骤3：解锁
                lock.unlock();
                log.info("线程{}解锁退出", Thread.currentThread().getName());
            }
        } else {
            log.info("线程{}未取得锁", Thread.currentThread().getName());
        }
    }

    /**
     * tryLock(long var1, long var3, TimeUnit var5) 接收3个参数，第一个指定最长等待时间waitTime，第二个指定最长持有锁的时间 holdTime, 第三个是单位
     */
    private static void lock1() {
        for (int i = 0; i < 3; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info(Thread.currentThread().getName() + " \t 运行");
                    RLock lock1 = redissonClient.getLock(KEY_LOCKED);
                    try {
                        // 尝试获取7s
//                        boolean b = lock1.tryLock(7,  TimeUnit.SECONDS);
                        // 尝试获取锁7s, 最多占有锁2s，超过后自动释放，调用unlock可以提前释放。
                        boolean b = lock1.tryLock(1, 7, TimeUnit.SECONDS);
                        if (!b) {
                            log.info(Thread.currentThread().getName() + " \t 获取锁失败");
                            return;
                        }
                    } catch (InterruptedException e) {
                    }

                    log.info(Thread.currentThread().getName() + " \t 获取锁");

                    try {
                        // 模拟处理逻辑用时
                        Thread.sleep((index * 7) * 1000);
                    } catch (InterruptedException e) {

                    }

                    // 如果是当前线程持有锁，手动释放
                    if (lock1.isHeldByCurrentThread()) {
                        lock1.unlock();
                        log.info(Thread.currentThread().getName() + " \t 释放锁");
                    }
                }
            }).start();
        }
    }


    /**
     * 默认使用的是非公平锁
     */
    private static void lock22() {
        for (int i = 0; i < 5; i++) {
            // 休眠一下使线程按照顺序启动
            try {
                Thread.sleep(1 * 100);
            } catch (InterruptedException e) {
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info(Thread.currentThread().getName() + " \t 运行");
                    //平锁
                    //RLock lock1 = redissonClient.getFairLock(KEY_LOCKED);
                    //下面方式获取到的是非公平锁
                    RLock lock1 = redissonClient.getLock(KEY_LOCKED);
//                    RLock lock1 = redissonClient.getFairLock(KEY_LOCKED);
                    log.error("lock1 clas: {}", lock1.getClass());
                    lock1.lock();

                    log.info(Thread.currentThread().getName() + " \t 获取锁");
                    try {
                        Thread.sleep(TIME_LOCKED);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    log.info(Thread.currentThread().getName() + " \t 释放锁");
                    lock1.unlock();
                }
            });
            thread.setName("MyThread: " + i);
            thread.start();
        }
    }


    /**
     * 读写锁
     */
    private static void lock4() {
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info(Thread.currentThread().getName() + " \t 运行");
                    RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(KEY_LOCKED);

                    readWriteLock.readLock().lock();
                    log.info(Thread.currentThread().getName() + " \t 获取读锁");
                    try {
                        // 模拟处理逻辑用时5s
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                    }
                    readWriteLock.readLock().unlock();
                    log.info(Thread.currentThread().getName() + " \t 释放读锁");

                    readWriteLock.writeLock().lock();
                    log.info(Thread.currentThread().getName() + " \t 获取写锁");
                    try {
                        // 模拟处理逻辑用时5s
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                    }
                    readWriteLock.writeLock().unlock();
                    log.info(Thread.currentThread().getName() + " \t 释放写锁");
                }
            }).start();
        }
    }


}
