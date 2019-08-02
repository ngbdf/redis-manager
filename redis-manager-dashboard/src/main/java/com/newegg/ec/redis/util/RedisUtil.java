package com.newegg.ec.redis.util;

import com.google.common.base.Strings;
import redis.clients.jedis.HostAndPort;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class RedisUtil {

    private RedisUtil() {}

    /**
     * info => map
     * cluster info => map
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

    public static final Set<HostAndPort> nodesToSet(String nodes) {
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
}
