package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jay.H.Zou
 * @date 8/2/2019
 */
public class RedisClusterUtil {

    public static final String OK = "ok";

    public static final String FAIL = "fail";

    public static final String CLUSTER_STATE = "cluster_state";

    public static final String CLUSTER_SLOTS_ASSIGNED = "cluster_slots_assigned";

    public static final String CLUSTER_SLOT_OK = "cluster_slots_ok";

    public static final String CLUSTER_SLOTS_PFAIL = "cluster_slots_pfail";

    public static final String CLUSTER_SLOTS_FAIL = "cluster_slots_fail";

    public static final String CLUSTER_KNOWN_NODES = "cluster_known_nodes";

    public static final String CLUSTER_SIZE = "cluster_size";


    public static final Cluster parseClusterInfoToObject(String clusterInfo) throws IOException {
        JSONObject infoJSONObject = new JSONObject();
        Map<String, String> clusterInfoMap = RedisUtil.parseInfoToMap(clusterInfo);
        for (Map.Entry<String, String> entry : clusterInfoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
                continue;
            }
            String field = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            infoJSONObject.put(field, value);
        }

        Cluster.ClusterState state = Cluster.ClusterState.UNKNOWN;
        String clusterState = clusterInfoMap.get(CLUSTER_STATE);
        if (Objects.equals(clusterState, OK)) {
            state = Cluster.ClusterState.GOOD;
        } else if (Objects.equals(clusterState, FAIL)) {
            state = Cluster.ClusterState.BAD;
        }
        String stateField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, CLUSTER_STATE);
        infoJSONObject.put(stateField, state);
        Cluster cluster = infoJSONObject.toJavaObject(Cluster.class);
        return cluster;
    }

    public static final Map<RedisNode, List<RedisNode>> getClusterNodes(String nodes) {

        return null;
    }

}
