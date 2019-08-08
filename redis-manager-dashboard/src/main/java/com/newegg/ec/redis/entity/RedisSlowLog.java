package com.newegg.ec.redis.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.util.Slowlog;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class RedisSlowLog {

    private HostAndPort hostAndPort;

    private Timestamp dateTime;

    private long executionTime;

    private String type;

    private String command;

    public RedisSlowLog(HostAndPort hostAndPort, Slowlog slowlog) {
        this.hostAndPort = hostAndPort;
        this.dateTime = new Timestamp(slowlog.getTimeStamp());
        this.executionTime = slowlog.getExecutionTime();
        List<String> args = slowlog.getArgs();
        this.type = args.get(0);
        List<String> commands = args.subList(1, args.size());
        this.command = Joiner.on(" ").skipNulls().join(commands);
    }


    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
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

    @Override
    public String toString() {
        return "RedisSlowLog{" +
                "hostAndPort=" + hostAndPort +
                ", dateTime=" + dateTime +
                ", executionTime=" + executionTime +
                ", type='" + type + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
