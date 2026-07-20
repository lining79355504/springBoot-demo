package com.instruct;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class FullGcHistoMonitor {

    // 两次执行最小间隔（秒）
    private static final long MIN_INTERVAL_SECONDS = 60;
    private long lastExecTimestamp = 0;

    private final Map<String, Long> gcCountSnapshot = new HashMap<>();
    private final List<GarbageCollectorMXBean> oldGenCollectors = new ArrayList<>();
    private final String jmapPath;
    private final String pid;

    public FullGcHistoMonitor() {
        // 获取 PID
        String name = ManagementFactory.getRuntimeMXBean().getName(); // 格式：pid@hostname
        this.pid = name.split("@")[0];

        // 获取 JAVA_HOME
        String javaHome = System.getProperty("java.home");
        this.jmapPath = javaHome + "/bin/jmap";

        // 筛选老年代 GC 收集器（同前文逻辑）
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            String gcName = gcBean.getName();
            if (gcName.contains("MarkSweep")
                    || gcName.contains("ConcurrentMarkSweep")
                    || gcName.contains("G1 Old")
                    || gcName.contains("G1 Mixed")
                    || gcName.contains("Shenandoah Pauses")
                    || gcName.equals("ZGC")) {
                oldGenCollectors.add(gcBean);
                gcCountSnapshot.put(gcName, gcBean.getCollectionCount());
            }
        }
        // 未识别到老年代收集器时回退监控所有 GC
        if (oldGenCollectors.isEmpty()) {
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                oldGenCollectors.add(gcBean);
                gcCountSnapshot.put(gcBean.getName(), gcBean.getCollectionCount());
            }
        }
    }

    @PostConstruct
    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fullgc-histo-monitor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::checkAndExecute, 5, 5, TimeUnit.SECONDS);
    }

    private void checkAndExecute() {
        for (GarbageCollectorMXBean gcBean : oldGenCollectors) {
            String gcName = gcBean.getName();
            long currentCount = gcBean.getCollectionCount();
            long lastCount = gcCountSnapshot.getOrDefault(gcName, 0L);

            if (currentCount > lastCount) {
                long now = System.currentTimeMillis();
                if (now - lastExecTimestamp > TimeUnit.SECONDS.toMillis(MIN_INTERVAL_SECONDS)) {
                    log.info("[GC-Monitor] 检测到 Full GC ({})，执行 jmap -histo:live...", gcName);
                    executeHistoCommand();
                    lastExecTimestamp = now;
                } else {
                    log.info("[GC-Monitor] Full GC 发生但距上次执行不足 {} 秒，跳过。", MIN_INTERVAL_SECONDS);
                }
                gcCountSnapshot.put(gcName, currentCount);
            }
        }
    }

    private void executeHistoCommand() {
        try {
            // 构建完整的 shell 命令
            String cmd = jmapPath + " -histo:live " + pid
                    + " | grep alibaba | sort -k 2 -g -r | head -20";
            log.info("[GC-Monitor] 执行命令: {}", cmd);

            // 使用 bash -c 以便支持管道
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
            pb.redirectErrorStream(true); // 将 stderr 合并到 stdout
            Process process = pb.start();

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("[GC-Monitor] 命令执行成功，输出：\n{}", output);
            } else {
                log.error("[GC-Monitor] 命令执行失败 (exit={})，输出：{}", exitCode, output);
            }
        } catch (Exception e) {
            log.error("[GC-Monitor] 执行 jmap 命令异常：{}", e.getMessage(), e);
        }
    }
}
