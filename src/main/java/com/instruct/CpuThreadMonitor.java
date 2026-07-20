package com.instruct;

import com.sun.management.OperatingSystemMXBean;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class CpuThreadMonitor {

    private static final double CPU_THRESHOLD = 0.01;
    private static final long SAMPLE_INTERVAL_MS = 200;
    private static final int TOP_N = 10;

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    {
        // 启用线程 CPU 计时（如果支持）
        if (threadMXBean instanceof com.sun.management.ThreadMXBean cmx) {
            cmx.setThreadCpuTimeEnabled(true);
        }
    }

    @PostConstruct
    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cpu-monitor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::monitor, 5, 10, TimeUnit.SECONDS);
    }

    private void monitor() {
        double usage = currentCpuUsage();
        log.info("[Monitor] 进程 CPU: {}%", String.format("%.1f", usage * 100));
        if (usage >= CPU_THRESHOLD) {
            log.info("[Monitor] 触发线程分析...");
            List<BusyThread> threads = getTopBusyThreads();
            for (BusyThread bt : threads) {
                log.info("TOP Thread: {} ({}) CPU: {}%\n{}",
                        bt.threadName, bt.threadId, String.format("%.2f", bt.cpuPercent), bt.stackTrace);
            }
        }
    }

    private double currentCpuUsage() {
        long t1 = System.nanoTime();
        long c1 = osBean.getProcessCpuTime();
        try { TimeUnit.MILLISECONDS.sleep(SAMPLE_INTERVAL_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        long t2 = System.nanoTime();
        long c2 = osBean.getProcessCpuTime();
        long elapsed = t2 - t1;
        long cpu = c2 - c1;
        return (cpu / (double) elapsed) / Runtime.getRuntime().availableProcessors();
    }

    private List<BusyThread> getTopBusyThreads() {
        long[] ids = threadMXBean.getAllThreadIds();
        Map<Long, Long> cpu1 = new HashMap<>(ids.length);
        for (long id : ids) {
            long t = threadMXBean.getThreadCpuTime(id);
            if (t >= 0) cpu1.put(id, t);
        }
        long procCpu1 = osBean.getProcessCpuTime();

        try { TimeUnit.MILLISECONDS.sleep(SAMPLE_INTERVAL_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return List.of(); }

        long procCpu2 = osBean.getProcessCpuTime();
        long procDelta = procCpu2 - procCpu1;
        if (procDelta == 0) return List.of();

        PriorityQueue<BusyThread> top = new PriorityQueue<>(
                Comparator.comparingDouble(BusyThread::getCpuPercent));
        for (long id : ids) {
            long t2 = threadMXBean.getThreadCpuTime(id);
            Long t1 = cpu1.get(id);
            if (t1 != null && t2 >= 0) {
                long delta = t2 - t1;
                double percent = (delta * 100.0) / procDelta;
                ThreadInfo info = threadMXBean.getThreadInfo(id, 20); // 最多 20 行栈
                if (info != null) {
                    BusyThread bt = new BusyThread(id, info.getThreadName(), percent, stackTraceToString(info.getStackTrace()));
                    top.offer(bt);
                    if (top.size() > TOP_N) top.poll();
                }
            }
        }
        List<BusyThread> result = new ArrayList<>(top);
        result.sort((a, b) -> Double.compare(b.cpuPercent, a.cpuPercent));
        return result;
    }

    private String stackTraceToString(StackTraceElement[] stack) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : stack) {
            sb.append("\tat ").append(e).append("\n");
        }
        return sb.toString();
    }

    static class BusyThread {
        long threadId;
        String threadName;
        double cpuPercent;
        String stackTrace;
        BusyThread(long id, String name, double pct, String stack) {
            this.threadId = id; this.threadName = name;
            this.cpuPercent = pct; this.stackTrace = stack;
        }
        double getCpuPercent() { return cpuPercent; }
    }
}