package com.newegg.ec.cache.app.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gl49 on 2018/6/14.
 */
public class ClusterImportResult {
    private String ownerIp;
    private int port;
    private String targetIp;
    private int targetPort;
    private String formatKey;
    private AtomicInteger importCount;

    public ClusterImportResult(String ownerIp, int port, String targetIp, int targetPort, String formatKey, AtomicInteger importCount) {
        this.ownerIp = ownerIp;
        this.port = port;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.formatKey = formatKey;
        this.importCount = importCount;
    }

    public String getOwnerIp() {
        return ownerIp;
    }

    public void setOwnerIp(String ownerIp) {
        this.ownerIp = ownerIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public String getFormatKey() {
        return formatKey;
    }

    public void setFormatKey(String formatKey) {
        this.formatKey = formatKey;
    }

    public AtomicInteger getImportCount() {
        return importCount;
    }

    public void setImportCount(AtomicInteger importCount) {
        this.importCount = importCount;
    }
}
