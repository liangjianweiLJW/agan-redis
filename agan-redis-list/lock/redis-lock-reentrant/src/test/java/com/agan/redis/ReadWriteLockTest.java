package com.agan.redis;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Description: 读写锁
 * @Author: jianweil
 * @date: 2022/1/27 20:04
 */
public class ReadWriteLockTest {

    private static ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    //读锁
    private static ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    //写锁
    private static ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();

    public static void read() {
        readLock.lock();
        try {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "获取读锁，开始执行");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "释放读锁");
            readLock.unlock();
        }
    }

    public static void write() {
        writeLock.lock();
        try {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "获取写锁，开始执行");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(LocalDateTime.now()+"    "+ Thread.currentThread().getName() + "释放写锁");
            writeLock.unlock();
        }
    }

    public static void main(String[] args) {
        new Thread(() -> read(), "Thread1").start();
        new Thread(() -> read(), "Thread2").start();
        new Thread(() -> write(), "Thread3").start();
        new Thread(() -> write(), "Thread4").start();
    }
}
