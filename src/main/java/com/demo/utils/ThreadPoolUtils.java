package com.demo.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author mort
 * @Description
 * @date 2023/5/17
 **/
public class ThreadPoolUtils {

    private static Object object;

    private static volatile Executor SINGLE_POOL
            = ThreadPoolCreateUtils.newCachedThreadPool("mort",
            10,
            30,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20), new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("mort" + "-%d")
//                    .setUncaughtExceptionHandler()
                    .build());


    public static Executor getSinglePool() {
        if (null != SINGLE_POOL) {
            return SINGLE_POOL;
        }

        synchronized (object) {
            if (null == SINGLE_POOL) {
                SINGLE_POOL
                        = ThreadPoolCreateUtils.newCachedThreadPool("mort",
                        10,
                        30,
                        60L,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(20), new ThreadFactoryBuilder()
                                .setDaemon(true)
                                .setNameFormat("mort" + "-%d")
//                    .setUncaughtExceptionHandler()
                                .build());
            }
            return SINGLE_POOL;
        }

    }

}
