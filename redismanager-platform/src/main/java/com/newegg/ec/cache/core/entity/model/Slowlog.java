package com.newegg.ec.cache.core.entity.model;

/**
 * Created by lf52 on 2019/2/26.
 *
 * from jedis : redis.clients.util.Slowlog
 */
import java.util.ArrayList;
import java.util.List;

public class Slowlog {
    private final long id;
    private final long timeStamp;
    private final long executionTime;
    private final List<String> args;
    private static final String COMMA = ",";

    @SuppressWarnings("unchecked")
    private Slowlog(List<Object> properties) {
        super();
        this.id = (Long) properties.get(0);
        this.timeStamp = (Long) properties.get(1);
        this.executionTime = (Long) properties.get(2);

        this.args = (List<String>) properties.get(3);
    }

    @SuppressWarnings("unchecked")
    public static List<Slowlog> from(List<Object> nestedMultiBulkReply) {
        List<Slowlog> logs = new ArrayList<Slowlog>(nestedMultiBulkReply.size());
        for (Object obj : nestedMultiBulkReply) {
            List<Object> properties = (List<Object>) obj;
            logs.add(new Slowlog(properties));
        }

        return logs;
    }

    public long getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public List<String> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(id).append(COMMA).append(timeStamp).append(COMMA)
                .append(executionTime).append(COMMA).append(args).toString();
    }
}
