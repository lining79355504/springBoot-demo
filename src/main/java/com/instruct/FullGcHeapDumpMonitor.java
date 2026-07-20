package com.instruct;

import com.sun.management.HotSpotDiagnosticMXBean;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class FullGcHeapDumpMonitor {

    // 两次 dump 最小间隔（秒）
    private static final long MIN_DUMP_INTERVAL_SECONDS = 60;
    private long lastDumpTimestamp = 0;

    // 记录每个老年代 GC 收集器的上一次收集次数
    private final Map<String, Long> gcCountSnapshot = new HashMap<>();
    private final List<GarbageCollectorMXBean> oldGenCollectors = new ArrayList<>();
    private final HotSpotDiagnosticMXBean diagnosticBean;

    public FullGcHeapDumpMonitor() {
        // 获取 HotSpot 诊断 MXBean（用于 dump heap）
        try {
            diagnosticBean = ManagementFactory.newPlatformMXBeanProxy(
                    ManagementFactory.getPlatformMBeanServer(),
                    "com.sun.management:type=HotSpotDiagnostic",
                    HotSpotDiagnosticMXBean.class);
        } catch (Exception e) {
            throw new RuntimeException("无法获取 HotSpotDiagnosticMXBean，当前 JVM 不支持 heap dump", e);
        }

        // 筛选出老年代 GC 收集器
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = gcBean.getName();
            // 以下名称覆盖 HotSpot 的各种老年代收集器
            if (name.contains("MarkSweep")          // Serial Old, Parallel Old
                    || name.contains("ConcurrentMarkSweep")  // CMS
                    || name.contains("G1 Old")               // G1
                    || name.contains("G1 Mixed")             // G1 混合回收（也处理老年代）
                    || name.contains("Shenandoah Pauses")     // Shenandoah
                    || name.equals("ZGC")) {                  // ZGC 全堆 GC
                oldGenCollectors.add(gcBean);
                gcCountSnapshot.put(name, gcBean.getCollectionCount());
            }
        }

        // 若未识别到任何特定老年代收集器，则回退为监控所有 GC（可能包含 Young GC，不够精确）
        if (oldGenCollectors.isEmpty()) {
            oldGenCollectors.addAll(ManagementFactory.getGarbageCollectorMXBeans());
            for (GarbageCollectorMXBean gcBean : oldGenCollectors) {
                gcCountSnapshot.put(gcBean.getName(), gcBean.getCollectionCount());
            }
        }
    }

    @PostConstruct
    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fullgc-monitor");
            t.setDaemon(true);
            return t;
        });
        // 每 5 秒检查一次
        scheduler.scheduleWithFixedDelay(this::checkAndDump, 5, 5, TimeUnit.SECONDS);
    }

    private void checkAndDump() {
        for (GarbageCollectorMXBean gcBean : oldGenCollectors) {
            String name = gcBean.getName();
            long currentCount = gcBean.getCollectionCount();
            long lastCount = gcCountSnapshot.getOrDefault(name, 0L);

            if (currentCount > lastCount) {
                long now = System.currentTimeMillis();
                if (now - lastDumpTimestamp > TimeUnit.SECONDS.toMillis(MIN_DUMP_INTERVAL_SECONDS)) {
                    log.info("[GC-Monitor] 检测到 Full GC (收集器={})，开始 heap dump...", name);
                    performHeapDump();
                    lastDumpTimestamp = now;
                } else {
                    log.info("[GC-Monitor] 发生 Full GC 但距离上次 dump 不足 {} 秒，跳过。", MIN_DUMP_INTERVAL_SECONDS);
                }
                // 更新快照计数
                gcCountSnapshot.put(name, currentCount);
            }
        }
    }

    private void performHeapDump() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            // 文件生成在当前工作目录，可改为绝对路径（如 /tmp/dump/heapdump-fullgc-xxx.hprof）
            String fileName = "heapdump-fullgc-" + timestamp + ".hprof";
            // 参数 live=false：直接 dump，不触发额外 GC；live=true：dump 前先 Full GC，只保留活对象
            diagnosticBean.dumpHeap(fileName, false);
            log.info("[GC-Monitor] Heap dump 成功：{}", fileName);
        } catch (Exception e) {
            log.error("[GC-Monitor] Heap dump 失败：{}", e.getMessage(), e);
        }
    }
}