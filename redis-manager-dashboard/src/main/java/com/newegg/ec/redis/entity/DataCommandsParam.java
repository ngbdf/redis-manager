package com.newegg.ec.redis.entity;



/**
 * @author Jay
 */
public class DataCommandsParam {

    private int clusterId;

    private int database;

    private String command;

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
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

}