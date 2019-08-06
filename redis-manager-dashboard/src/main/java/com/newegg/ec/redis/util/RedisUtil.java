package com.newegg.ec.redis.util;

import com.google.common.base.Strings;
import redis.clients.jedis.HostAndPort;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class RedisUtil {

    public static final String IP = "ip";

    public static final String PORT = "port";

    private RedisUtil() {
    }

    /**
     * info => map
     * cluster info => map
     *
     * @param info
     * @return
     * @throws IOException
     */
    public static final Map<String, String> parseInfoToMap(String info) throws IOException {
        Map<String, String> infoMap = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(info.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] keyValue = line.split(":");
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0];
            String value = keyValue[1];
            if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
                continue;
            }
            infoMap.put(key, value);
        }
        return infoMap;
    }

    public static final Set<HostAndPort> nodesToHostAndPortSet(String nodes) {
        String[] nodesArr = nodes.split(",");
        int length = nodesArr.length;
        Set<HostAndPort> hostAndPortSet = new HashSet<>(length);
        if (length > 0) {
            for (String node : nodesArr) {
                String[] ipAndPort = node.split(":");
                HostAndPort hostAndPort = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
                hostAndPortSet.add(hostAndPort);
            }
        }
        return hostAndPortSet;
    }

    public static BigDecimal avg(List<BigDecimal> bigDecimalList) {
        BigDecimal summation = new BigDecimal(bigDecimalList.size());
        BigDecimal size = new BigDecimal(bigDecimalList.size());
        for (BigDecimal bigDecimal : bigDecimalList) {
            summation.add(bigDecimal);
        }
        return summation.divide(size);
    }

    public static BigDecimal max(List<BigDecimal> bigDecimalList) {
        BigDecimal maxBigDecimal = null;
        for (BigDecimal bigDecimal : bigDecimalList) {
            if (maxBigDecimal == null) {
                maxBigDecimal = bigDecimal;
                continue;
            }
            maxBigDecimal = maxBigDecimal.max(bigDecimal);
        }
        return maxBigDecimal;
    }

    public static BigDecimal min(List<BigDecimal> bigDecimalList) {
        BigDecimal minBigDecimal = null;
        for (BigDecimal bigDecimal : bigDecimalList) {
            if (minBigDecimal == null) {
                minBigDecimal = bigDecimal;
                continue;
            }
            minBigDecimal = minBigDecimal.min(bigDecimal);
        }
        return minBigDecimal;
    }
}
