package com.demo.warp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 上下文感知的Runnable装饰器
 * <p>
 * 用于在多线程环境中传递主线程的上下文信息到子线程
 */
public class ContextAwareRunnable implements Runnable {

    private final Runnable target;
    private final String context; // 存储主线程的上下文

    public ContextAwareRunnable(Runnable target) {
        this.target = target;
        // 捕获当前线程（主线程）的上下文
        this.context = ContextHolder.getContext();
    }

    @Override
    public void run() {
        String originContext = ContextHolder.getContext();
        try {
            // 设置子线程的上下文为主线程的上下文
            ContextHolder.setContext(context);
            target.run();
        } finally {
            ContextHolder.setContext(originContext); // 恢复原有上下文
        }
    }

    private void test() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(new ContextAwareRunnable(() -> {
            System.out.println("子线程获取到的上下文：" + ContextHolder.getContext());
        }));
    }
}


/**
 * // 使用方式：提交任务时用装饰器包装
 * <p>
 * ExecutorService executor = Executors.newFixedThreadPool(5);
 * executor.submit(new ContextAwareRunnable(() -> {
 * System.out.println("子线程获取到的上下文：" + ContextHolder.getContext());
 * }));
 */
