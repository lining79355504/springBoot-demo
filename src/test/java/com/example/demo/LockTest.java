package com.example.demo;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author mort
 * @Description
 * @date 2023/5/23
 *
 * wait 会释放锁 其他线程可以继续获取 必须和synchronized一起用
 * notify 会唤醒一个线程
 * notifyAll 会唤醒全部线程
 **/

@Slf4j
public class LockTest {

    public static void main(String[] args) {
        Object lock = new Object();
        Thread threadA = new Thread(() -> {
            synchronized (lock) {
                log.info("A获取了锁");
                try {
                    log.info("A休眠一会儿");
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("A调用wait..");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                log.info("A被唤醒");
            }
        }, "A");
        threadA.start();

        Thread threadB = new Thread(()->{
            synchronized (lock) {
                log.info("B获得了锁");

                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                log.info("叫醒A");
//                lock.notify();

                log.info("叫醒B");
//                lock.notify();
            }
        }, "B");
        threadB.start();

        Thread threadC = new Thread(()->{
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lock){

                lock.notifyAll();
            }
        }, "C");

       threadC.start();
    }
}
