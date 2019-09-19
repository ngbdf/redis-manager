package com.newegg.ec.redis.entity;



/**
 * @author Jay
 */
public class DataCommandsParam {

    private Integer clusterId;

    private int database;

    private String command;

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "DataCommandsParam{" +
                "clusterId=" + clusterId +
                ", database=" + database +
                ", command='" + command + '\'' +
                '}';
    }
}