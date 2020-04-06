package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * TODO: move to clusterService
 *
 * @author Jay.H.Zou
 * @date 8/2/2019
 */
public class RedisClusterInfoUtil {

    public static final String OK = "ok";

    public static final String FAIL = "fail";

    public static final String CLUSTER_STATE = "cluster_state";

    public static final String CLUSTER_SLOTS_ASSIGNED = "cluster_slots_assigned";

    public static final String CLUSTER_SLOT_OK = "cluster_slots_ok";

    public static final String CLUSTER_SLOTS_PFAIL = "cluster_slots_pfail";

    public static final String CLUSTER_SLOTS_FAIL = "cluster_slots_fail";

    public static final String CLUSTER_KNOWN_NODES = "cluster_known_nodes";

    public static final String CLUSTER_SIZE = "cluster_size";


    public static Cluster parseClusterInfoToObject(Map<String, String> clusterInfoMap) throws IOException {
        JSONObject infoJSONObject = new JSONObject();
        for (Map.Entry<String, String> entry : clusterInfoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
                continue;
            }
            String field = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            // 计算集群状态
            if (Objects.equals(key, CLUSTER_STATE)) {
                Cluster.ClusterState clusterState = calculateClusterState(value);
                infoJSONObject.put(field, clusterState);
                continue;
            }
            infoJSONObject.put(field, value);
        }
        return infoJSONObject.toJavaObject(Cluster.class);
    }

    /**
     * 目前以 cluster info 为标准
     *
     * @param clusterState
     * @return
     */
    private static Cluster.ClusterState calculateClusterState(String clusterState) {
        return Objects.equals(clusterState, OK) ? Cluster.ClusterState.HEALTH : Cluster.ClusterState.BAD;
    }

}
