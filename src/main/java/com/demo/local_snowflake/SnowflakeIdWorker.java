package com.demo.local_snowflake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Snowflake分布式ID生成器
 *
 * ID结构 (64位):
 * 0 - 1位标识位，始终为0
 * 1 - 41位时间戳(毫秒级)，可使用约69年
 * 42 - 46位数据中心ID，支持32个数据中心
 * 47 - 51位工作机器ID，支持32个机器
 * 52 - 63位序列号，毫秒内支持4096个ID
 */
public class SnowflakeIdWorker {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeIdWorker.class);

    /**
     * 开始时间截 (2017-08-15)
     */
    private final long twepoch = 1502726400000L;

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~31)
     */
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    //==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long datacenterId) {
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = getWorkerIdFromIP();
        this.datacenterId = datacenterId;
        log.info("SnowflakeIdWorker initialized: datacenterId={}, workerId={}", datacenterId, workerId);
    }

    /**
     * 从IP地址获取workerId
     * 优先使用非回环的网卡IP，使用IP最后一个字节的低5位作为workerId
     *
     * @return workerId (0~31)
     */
    private long getWorkerIdFromIP() {
        try {
            InetAddress ip = getLocalHostAddress();
            if (ip != null) {
                byte[] ipBytes = ip.getAddress();
                long workerId = ipBytes[ipBytes.length - 1] & maxWorkerId;
                log.debug("Derived workerId from IP {}: {}", ip.getHostAddress(), workerId);
                return workerId;
            }
        } catch (Exception e) {
            log.warn("Failed to get workerId from IP, using default 0", e);
        }
        return 0L;
    }

    /**
     * 获取本机IP地址（优先非回环地址）
     *
     * @return 本机IP地址
     * @throws SocketException
     * @throws UnknownHostException
     */
    private InetAddress getLocalHostAddress() throws SocketException, UnknownHostException {
        // 优先使用非回环地址
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                // 优先使用IPv4地址
                if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address.getAddress().length == 4) {
                    return address;
                }
            }
        }
        // 回退到默认方式
        return InetAddress.getLocalHost();
    }


    // ==============================Methods==========================================

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (datacenterId << datacenterIdShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    //==============================Getters==========================================

    /**
     * 获取当前数据中心ID
     *
     * @return 数据中心ID
     */
    public long getDatacenterId() {
        return datacenterId;
    }

    /**
     * 获取当前工作机器ID
     *
     * @return 工作机器ID
     */
    public long getWorkerId() {
        return workerId;
    }

    public static void main(String[] args) {
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0);
        idWorker.nextId();
    }
}
