package com.zbkj.admin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.sun.management.OperatingSystemMXBean;
import com.zbkj.admin.service.SystemStateService;
import com.zbkj.common.response.system.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统状态服务实现类
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Slf4j
@Service
public class SystemStateServiceImpl implements SystemStateService {

    /**
     * 获取系统状态
     */
    @Override
    public SystemStateInfoResponse getIngo() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        CpuInfoResponse cpuInfoResponse = new CpuInfoResponse();
        // 获取CPU核心数
        int availableProcessors = osBean.getAvailableProcessors();
        cpuInfoResponse.setCoreNum(availableProcessors);
        // 获取用户使用率、系统使用率、当前空闲率
        double systemCpuLoad = osBean.getSystemCpuLoad() * 100; // 系统使用率
        double processCpuLoad = osBean.getProcessCpuLoad() * 100; // 用户使用率
        double idleRate = 100 - systemCpuLoad; // 当前空闲率
        cpuInfoResponse.setUserUsageRate(String.format("%.2f", processCpuLoad) + "%");
        cpuInfoResponse.setSystemUsageRate(String.format("%.2f", systemCpuLoad) + "%");
        cpuInfoResponse.setIdleRate(String.format("%.2f", idleRate) + "%");


        MemoryInfoResponse memoryInfoResponse = new MemoryInfoResponse();
        // 获取总内存（单位：字节）
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        // 获取已用内存（单位：字节）
        long usedMemory = totalMemory - osBean.getFreePhysicalMemorySize();
        // 获取剩余内存（单位：字节）
        long freeMemory = osBean.getFreePhysicalMemorySize();
        // 计算内存使用率
        double memoryUsage = (double) usedMemory / totalMemory * 100;
        // 转换为GB
        double totalMemoryGB = totalMemory / (1024.0 * 1024 * 1024);
        double usedMemoryGB = usedMemory / (1024.0 * 1024 * 1024);
        double freeMemoryGB = freeMemory / (1024.0 * 1024 * 1024);
        // 拼接内存信息
        memoryInfoResponse.setMemoryTotalMemory(String.format("%.2f", totalMemoryGB) + "G");
        memoryInfoResponse.setMemoryUsedMemory(String.format("%.2f", usedMemoryGB) + "G");
        memoryInfoResponse.setMemoryRemainingMemory(String.format("%.2f", freeMemoryGB) + "G");
        memoryInfoResponse.setMemoryUsageRate(String.format("%.2f", memoryUsage) + "%");
        // 获取JVM内存信息
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemory = runtime.totalMemory();
        long jvmFreeMemory = runtime.freeMemory();
        long jvmUsedMemory = jvmTotalMemory - jvmFreeMemory;
        double jvmMemoryUsage = (double) jvmUsedMemory / jvmTotalMemory * 100;
        memoryInfoResponse.setJvmTotalMemory(String.format("%.2f", jvmTotalMemory / (1024.0 * 1024)) + "M");
        memoryInfoResponse.setJvmUsedMemory(String.format("%.2f", jvmUsedMemory / (1024.0 * 1024)) + "M");
        memoryInfoResponse.setJvmRemainingMemory(String.format("%.2f", jvmFreeMemory / (1024.0 * 1024)) + "M");
        memoryInfoResponse.setJvmUsageRate(String.format("%.2f", jvmMemoryUsage) + "%");

        ServerInfoResponse serverInfoResponse = new ServerInfoResponse();
        // 获取服务器IP地址
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            serverInfoResponse.setServerName(localHost.getHostName());
            serverInfoResponse.setServerIp(localHost.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
//            throw new CrmebException(CommonResultCode.ERROR, "获取服务器IP地址失败");
        }
        serverInfoResponse.setOperatingSystem(System.getProperty("os.name"));
        String arch = osBean.getArch();
        serverInfoResponse.setSystemArchitecture(arch);

        JvmInfoResponse jvmInfoResponse = new JvmInfoResponse();
        jvmInfoResponse.setJavaName(System.getProperty("java.vm.name"));
        jvmInfoResponse.setJavaVersion(System.getProperty("java.version"));
        // 获取JVM启动时间和运行时长
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long startTime = runtimeMXBean.getStartTime(); // 启动时间（毫秒）
        long uptime = runtimeMXBean.getUptime(); // 运行时长（毫秒）
        jvmInfoResponse.setStartTime(DateUtil.date(new Date(startTime)).toString());
        jvmInfoResponse.setRunningTime(formatUptime(uptime));
        jvmInfoResponse.setInstallationPath(System.getProperty("java.home"));
        jvmInfoResponse.setProjectPath(System.getProperty("user.dir"));
        // 获取JVM运行参数
        List<String> inputArguments = runtimeMXBean.getInputArguments();
        jvmInfoResponse.setOperatingParameters(inputArguments.toString());

        List<DiskInfoResponse> diskInfoResponseList = new ArrayList<>();
        // 遍历所有磁盘分区
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            DiskInfoResponse diskInfoResponse = new DiskInfoResponse();
            try {
                FileStore store = Files.getFileStore(root);
                long totalSpace = store.getTotalSpace(); // 总空间
                long usableSpace = store.getUsableSpace(); // 可用空间
                long usedSpace = totalSpace - usableSpace; // 已用空间
                double usageRate = (double) usedSpace / totalSpace * 100; // 使用率

                // 转换为GB
                double totalSpaceGB = totalSpace / (1024.0 * 1024 * 1024);
                double usedSpaceGB = usedSpace / (1024.0 * 1024 * 1024);
                double usableSpaceGB = usableSpace / (1024.0 * 1024 * 1024);

                // 获取文件系统和盘符类型
                String fileSystem = store.type(); // 文件系统类型
                String driveType = getDriveType(root); // 盘符类型

                // 拼接磁盘信息
                diskInfoResponse.setDriveLetterPath(root.toString());
                diskInfoResponse.setFileSystem(fileSystem);
                diskInfoResponse.setDriveLetterType(driveType);
                diskInfoResponse.setTotalSize(String.format("%.2f", totalSpaceGB) + "GB");
                diskInfoResponse.setUsableSize(String.format("%.2f", usableSpaceGB) + "GB");
                diskInfoResponse.setUsedSize(String.format("%.2f", usedSpaceGB) + "GB");
                diskInfoResponse.setUsedRate(String.format("%.2f", usageRate) + "%");

            } catch (IOException e) {
                e.printStackTrace();
            }
            diskInfoResponseList.add(diskInfoResponse);
        }

        SystemStateInfoResponse response = new SystemStateInfoResponse();
        response.setCpuInfo(cpuInfoResponse);
        response.setMemoryInfo(memoryInfoResponse);
        response.setServerInfo(serverInfoResponse);
        response.setJvmInfo(jvmInfoResponse);
        response.setDiskInfoList(diskInfoResponseList);
        return response;
    }

    /**
     * 格式化运行时长
     *
     * @param uptime 运行时长（毫秒）
     * @return 格式化后的字符串（如 "1天 2小时 3分钟 4秒"）
     */
    private static String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%d天 %d小时 %d分钟 %d秒", days, hours, minutes, secs);
    }

    /**
     * 获取盘符类型
     *
     * @param path 磁盘路径
     * @return 盘符类型（如 "本地磁盘" 或 "网络驱动器"）
     */
    private static String getDriveType(Path path) {
        try {
            FileStore store = Files.getFileStore(path);
            String fileSystemType = store.type();

            // Windows 系统
            if (fileSystemType.equalsIgnoreCase("NTFS") || fileSystemType.equalsIgnoreCase("FAT32")) {
                return "本地磁盘";
            } else if (fileSystemType.equalsIgnoreCase("NFS") || fileSystemType.equalsIgnoreCase("CIFS")) {
                return "网络驱动器";
            }
            // Linux 系统
            else if (fileSystemType.equalsIgnoreCase("ext4") || fileSystemType.equalsIgnoreCase("xfs")) {
                return "Linux 分区";
            } else {
                return "未知类型";
            }
        } catch (IOException e) {
            return "未知类型";
        }
    }
}
