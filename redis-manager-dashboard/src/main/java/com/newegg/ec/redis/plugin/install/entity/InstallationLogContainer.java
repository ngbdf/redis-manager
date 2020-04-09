package com.newegg.ec.redis.plugin.install.entity;

import com.google.common.base.Strings;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * TODO: 并发问题
 *
 * @author Jay.H.Zou
 * @date 4/5/2020
 */
public class InstallationLogContainer {


    private static final Map<String, BlockingDeque<String>> INSTALLATION_LOG = new ConcurrentHashMap<>();

    public static boolean isNotUsed(String clusterName) {
        return !INSTALLATION_LOG.containsKey(clusterName);
    }

    public static void remove(String clusterName) {
        INSTALLATION_LOG.remove(clusterName);
    }

    public static void appendLog(String clusterName, String message) {
        BlockingDeque<String> logQueue = getLogDeque(clusterName);
        if (!Strings.isNullOrEmpty(message)) {
            logQueue.add("[Installation] " + message);
        }
    }


    public static List<String> getLogs(String clusterName) {
        List<String> logs = new LinkedList<>();
        BlockingDeque<String> logContainer = getLogDeque(clusterName);
        try {
            while (!logContainer.isEmpty()) {
                String log = logContainer.pollFirst(1, TimeUnit.SECONDS);
                if (!Strings.isNullOrEmpty(log)) {
                    logs.add(log);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return logs;
    }


    private static BlockingDeque<String> getLogDeque(String clusterName) {
        BlockingDeque<String> logDeque = INSTALLATION_LOG.get(clusterName);
        if (logDeque == null) {
            logDeque = new LinkedBlockingDeque<>();
        }
        INSTALLATION_LOG.put(clusterName, logDeque);
        return logDeque;
    }

}