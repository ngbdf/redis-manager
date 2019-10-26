package com.newegg.ec.redis.util;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.RedisNode;
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

    public static final String STANDALONE = "standalone";

    public static final String CLUSTER = "cluster";

    /**
     * nodes 相关
     */
    public static final String IP = "ip";

    public static final String PORT = "port";

    public static final String ROLE = "role";

    /**
     * 还未取得信任的节点，当前正在与其进行握手
     */
    public static final String HANDSHAKE = "handshake";

    /**
     * 节点处于FAIL 状态, 大部分节点都无法与其取得联系将会将改节点由 PFAIL 状态升级至FAIL状态
     */
    public static final String FAIL = "fail";

    /**
     * 节点处于PFAIL 状态, 当前节点无法联系，但逻辑上是可达的 (非 FAIL 状态).
     */
    public static final String PFAIL = "fail?";

    /**
     * 没有地址的节点(No address known for this node)
     */
    public static final String NOADDR = "noaddr";

    /**
     * 没有地址的节点(No address known for this node)
     */
    public static final String NOFLAGS = "noflags";

    public static final String MASTER_HOST = "master_host";

    public static final String MASTER_PORT = "master_port";

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
        Map<String, String> infoMap = new LinkedHashMap<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(info.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] keyValue = SignUtil.splitByColon(line);
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
        String[] nodeList = SignUtil.splitByCommas(nodes);
        int length = nodeList.length;
        Set<HostAndPort> hostAndPortSet = new HashSet<>(length);
        if (length > 0) {
            for (String node : nodeList) {
                String[] ipAndPort = SignUtil.splitByColon(node);
                HostAndPort hostAndPort = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
                hostAndPortSet.add(hostAndPort);
            }
        }
        return hostAndPortSet;
    }

    public static final HostAndPort nodesToHostAndPort(String node) {
        Set<HostAndPort> hostAndPortSet = nodesToHostAndPortSet(node);
        return hostAndPortSet.iterator().next();
    }

    public static BigDecimal avg(List<BigDecimal> bigDecimalList) {
        BigDecimal summation = new BigDecimal(0);
        BigDecimal size = new BigDecimal(bigDecimalList.size());
        if (size.intValue() == 0) {
            return summation;
        }
        for (BigDecimal bigDecimal : bigDecimalList) {
            summation = summation.add(bigDecimal);
        }

        return summation.divide(size, 2, BigDecimal.ROUND_HALF_UP);
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

    public static String[] removeCommandAndKey(String[] list) {
        int length = list.length;
        String[] items = new String[length - 2];
        for (int i = 2, j = 0; i < length; i++, j++) {
            items[j] = list[i];
        }
        return items;
    }
    public static String getKey(String command) {
        return SignUtil.splitBySpace(command)[1];
    }

    public static boolean equals(RedisNode redisNode1, RedisNode redisNode2) {
        return Objects.equals(redisNode1.getHost(), redisNode2.getHost())
                && redisNode1.getPort() == redisNode2.getPort();
    }

    public static String getNodeString(RedisNode redisNode) {
        StringBuilder result = new StringBuilder();
        result.append(redisNode.getHost()).append(SignUtil.COLON).append(redisNode.getPort());
        return result.toString();
    }

}
