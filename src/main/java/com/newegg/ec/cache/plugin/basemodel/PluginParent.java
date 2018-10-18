package com.newegg.ec.cache.plugin.basemodel;

import com.newegg.ec.cache.app.component.RedisManager;
import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/5/9.
 */
@Component
public abstract class PluginParent {
    public static final String IPLIST_NAME = "iplist";
    public static final String IMAGE = "image";
    @Resource
    protected ClusterLogic clusterLogic;

    @Resource
    private RedisManager redisManager;

    public boolean installTemplate(PluginParent pluginParent, JSONObject reqParam) {
        //重新检查一下各种权限
        boolean checkReq = pluginParent.checkInstall(reqParam);
        if (!checkReq) {
            return false;
        }
        String ipListStr = reqParam.getString(IPLIST_NAME);
        Map<RedisNode, List<RedisNode>> ipMap = JedisUtil.getInstallNodeMap(ipListStr);
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        // 安装节点
        pluginParent.installNodeList(reqParam, nodelist);
        // 判断节点是否成功
        boolean checkRes = pluginParent.checkInstallResult(nodelist);
        int clusterId = 0;
        if (checkRes) { // 如果安装成功
            if (reqParam.containsKey("clusterId")) {  //如果有传 clusterId 那么证明是从扩容界面来的
                clusterId = reqParam.getInt("clusterId");
            } else {
                clusterId = pluginParent.addCluster(reqParam);
            }
            if (clusterId != -1) {
                pluginParent.addNodeList(reqParam, clusterId);
            }
            // 建立集群
            pluginParent.buildRedisCluster(clusterId, ipMap);
        }
        return checkRes;
    }

    protected abstract boolean checkInstall(JSONObject reqParam);

    protected abstract void addNodeList(JSONObject reqParam, int clusterId);

    protected abstract void installNodeList(JSONObject reqParam, List<RedisNode> nodelist);

    /**
     * table cluster 写入数据
     *
     * @param reqParam
     * @return 返回-1 写入数据失败
     */
    protected int addCluster(JSONObject reqParam) {
        int clusterId = -1;
        String ipListStr = reqParam.getString(IPLIST_NAME);
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        RedisNode node = new RedisNode();
        for (RedisNode redisNode : nodelist) {
            if (NetUtil.checkIpAndPort(redisNode.getIp(), redisNode.getPort())) {
                node = redisNode;
                break;
            }
        }
        if (StringUtils.isNotEmpty(node.getIp())) {
            Cluster cluster = new Cluster();
            cluster.setAddress(node.getIp() + ":" + node.getPort());
            cluster.setUserGroup(reqParam.get("userGroup").toString());
            cluster.setClusterType(reqParam.get("pluginType").toString());
            cluster.setClusterName(reqParam.get("clusterName").toString());
            clusterId = clusterLogic.addCluster(cluster);
        }
        return clusterId;
    }

    protected void buildRedisCluster(int clusterId, Map<RedisNode, List<RedisNode>> ipMap) {
        redisManager.buildCluster(clusterId, ipMap);
    }

    protected boolean checkInstallResult(List<RedisNode> ipList) {
        boolean res = false;
        for (RedisNode redisNode : ipList) {
            res = NetUtil.checkIpAndPort(redisNode.getIp(), redisNode.getPort());
            if (res) {
                break;
            }
        }
        return res;
    }
}
