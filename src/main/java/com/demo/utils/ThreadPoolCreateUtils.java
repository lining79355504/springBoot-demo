package com.demo.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mort
 * @Description
 * @date 2023/5/17
 **/
@Slf4j
public class ThreadPoolCreateUtils {

    static final RejectedExecutionHandler DEFAULT_HANDLER = new ThreadPoolExecutor.AbortPolicy();

    /**
     * 默认阻塞系数
     */
    private static final double DEFAULT_BLOCKING_COEFFICIENT = 0.6;

    /**
     * 默认线程池并发度
     * 核数 / (1 - 阻塞系数)
     *
     *
     */
    private static final int DEFAULT_PARALLELISM = (int) (Runtime.getRuntime().availableProcessors() / (1 - DEFAULT_BLOCKING_COEFFICIENT));

    public static final ForkJoinPool common;

    //通过反射 修改 jdk8  CompletableFuture.runAsync 等使用的线程池  parallelStream
    static {
        common = newForkJoinPool("Fork-Join-Common-Pool", DEFAULT_PARALLELISM);
        try {
            Field field = ForkJoinPool.class.getDeclaredField("common");
            field.setAccessible(true);
            boolean isFinal = Modifier.isFinal(field.getModifiers());
            Field modifiers = field.getClass().getDeclaredField("modifiers");
            if (isFinal) {
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.set(null, common);
            if (isFinal) {
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Can not find declared field: common", e);
        }
    }


    /**
     * Create new thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @return executor
     */
    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return newThreadPoolExecutor(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, true);
    }

    /**
     * Create new thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param daemon
     * @return executor
     */
    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, boolean daemon) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param handler
     * @return executor
     */
    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                           RejectedExecutionHandler handler) {
        return newThreadPoolExecutor(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler, true);
    }

    /**
     * Create new thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param handler
     * @param daemon
     * @return executor
     */
    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                           RejectedExecutionHandler handler, boolean daemon) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, getFactory(name, daemon), handler);
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new scheduled thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @return executor
     * @see Executors#newScheduledThreadPool(int)
     */
    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize) {
        return newScheduledThreadPool(name, corePoolSize, true);
    }

    /**
     * Create new scheduled thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param daemon
     * @return executor
     * @see Executors#newScheduledThreadPool(int, ThreadFactory)
     */
    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize, boolean daemon) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new scheduled thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param handler
     * @return executor
     * @see Executors#newScheduledThreadPool(int)
     */
    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize, RejectedExecutionHandler handler) {
        return newScheduledThreadPool(name, corePoolSize, handler, true);
    }

    /**
     * Create new scheduled thread poll executor
     *
     * @param name
     * @param corePoolSize
     * @param handler
     * @param daemon
     * @return executor
     * @see Executors#newScheduledThreadPool(int, ThreadFactory)
     */
    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize, RejectedExecutionHandler handler, boolean daemon) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, getFactory(name, daemon), handler);
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new cached thread poll executor
     *
     * @param name
     * @return executor
     * @see Executors#newCachedThreadPool()
     */
    public static ThreadPoolExecutor newCachedThreadPool(String name) {
        return newCachedThreadPool(name, true);
    }

    /**
     * Create new cached thread poll executor
     *
     * @param name
     * @param daemon
     * @return executor
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    public static ThreadPoolExecutor newCachedThreadPool(String name, boolean daemon) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS, new SynchronousQueue<>(), getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new cached thread poll executor
     *
     * @param name
     * @param daemon
     * @return executor
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    public static ThreadPoolExecutor newCachedThreadPool(String name, boolean daemon,
                                                         int corePoolSize,
                                                         int maximumPoolSize,
                                                         long keepAliveTime,
                                                         TimeUnit unit,
                                                         BlockingQueue<Runnable> workQueue) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, workQueue, getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }


    /**
     *
     * @param name
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory
     * @return
     */
    public static ThreadPoolExecutor newCachedThreadPool(String name,
                                                         int corePoolSize,
                                                         int maximumPoolSize,
                                                         long keepAliveTime,
                                                         TimeUnit unit,
                                                         BlockingQueue<Runnable> workQueue,
                                                         ThreadFactory threadFactory) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, workQueue, threadFactory);
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new fixed thread poll executor
     *
     * @param name
     * @param nThreads
     * @return executor
     * @see Executors#newFixedThreadPool(int)
     */
    public static ThreadPoolExecutor newFixedThreadPool(String name, int nThreads) {
        return newFixedThreadPool(name, nThreads, true);
    }

    /**
     * Create new fixed thread poll executor
     *
     * @param name
     * @param nThreads
     * @param daemon
     * @return executor
     * @see Executors#newFixedThreadPool(int, ThreadFactory)
     */
    public static ThreadPoolExecutor newFixedThreadPool(String name, int nThreads, boolean daemon) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    /**
     * Create new work stealing pool
     *
     * @param name
     * @param parallelism
     * @return executor
     * @see Executors#newWorkStealingPool(int)
     */
    public static ForkJoinPool newForkJoinPool(String name, int parallelism) {
        ForkJoinPool forkJoin = new ForkJoinPool(parallelism, new ForkJoinThreadFactory(name), null, true);
        attachMonitor(name, forkJoin);
        return forkJoin;
    }

    /**
     * Create new work stealing pool
     *
     * @param name
     * @param parallelism
     * @param handler
     * @param asyncMode
     * @return executor
     * @see Executors#newWorkStealingPool(int)
     */
    public static ForkJoinPool newForkJoinPool(String name, int parallelism, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        ForkJoinPool forkJoin = new ForkJoinPool(parallelism, new ForkJoinThreadFactory(name), handler, asyncMode);
        attachMonitor(name, forkJoin);
        return forkJoin;
    }

    //重写 ThreadFactory 实现线程命名自增加1
    private static ThreadFactory getFactory(String name, boolean daemon) {
        return new ThreadFactoryBuilder()
                .setNameFormat(name + "-%d")
                .setDaemon(daemon)
                .build();
    }

    private static void attachMonitor(String name, AbstractExecutorService executor) {
        WeakReference<AbstractExecutorService> executorReference = new WeakReference<>(executor);
        ScheduleWithFixedDelayPoolUtils.scheduleWithFixedDelay(() -> {
            AbstractExecutorService currentExecutor = executorReference.get();
            if (currentExecutor == null) {
                throw new IllegalArgumentException("Current executor reference is null, cancel the task");
            }
            if (currentExecutor.isShutdown()) {
                throw new IllegalArgumentException("Current executor reference is shutdown, cancel the task");
            }
            if (currentExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor t = (ThreadPoolExecutor) currentExecutor;
                //监控线程
//                ThreadPool.POOL_STATE_ACTIVE.set(t.getActiveCount(), name); //返回正在积极执行任务的线程的大致数目
//                if (!(currentExecutor instanceof ScheduledThreadPoolExecutor)) {
//                    ThreadPool.POOL_STATE_TASK_WAITING.set(t.getQueue().size(), name); //提交到此池中，但未开始执行的任务数量
//                }
            }
            if (currentExecutor instanceof ForkJoinPool) {
                ForkJoinPool f = (ForkJoinPool) currentExecutor;
//                ThreadPool.POOL_STATE_ACTIVE.set(f.getActiveThreadCount(), name); //正在窃取或运行中的线程数
//                ThreadPool.POOL_STATE_TASK_WAITING.set(f.getQueuedSubmissionCount(), name); //提交到此池中，但未开始执行的任务数量
            }

        }, 1, 1, TimeUnit.SECONDS);
    }

    public static class ForkJoinThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private final String name;
        private final AtomicLong count = new AtomicLong();

        public ForkJoinThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new NamedForkJoinWorkerThread(String.format("%s-%d", name, count.incrementAndGet()), pool);
        }
    }


    public static class NamedForkJoinWorkerThread extends ForkJoinWorkerThread {
        protected NamedForkJoinWorkerThread(String name, ForkJoinPool pool) {
            super(pool);
            super.setName(name);
            super.setContextClassLoader(ClassLoader.getSystemClassLoader());
        }
    }
}
