package com.newegg.ec.cache.core.entity.redis;

/**
 * Created by lf52 on 2018/4/27.
 */
public class RedisSlowLog {

    public RedisSlowLog(){

    }

    /**
     * ipport
     */
    private String host;

    /**
     * now system time
     */
    private String showDate;

    /**
     * command run time
     */
    private long runTime;

    /**
     * command type
     */
    private String type;

    /**
     * show log command
     */
    private String command;

    /**
     * show log time
     */
    private long addTime;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    @Override
    public String toString() {
        return "RedisSlowLog{" +
                "host='" + host + '\'' +
                ", showDate='" + showDate + '\'' +
                ", runTime=" + runTime +
                ", type='" + type + '\'' +
                ", command='" + command + '\'' +
                ", addTime=" + addTime +
                '}';
    }

}
