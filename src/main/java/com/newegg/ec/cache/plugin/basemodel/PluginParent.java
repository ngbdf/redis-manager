package com.newegg.ec.cache.plugin.basemodel;

import com.newegg.ec.cache.app.component.RedisManager;
import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/5/9.
 */
@Component
public abstract class PluginParent {

    private static final Log logger = LogFactory.getLog(PluginParent.class);

    protected static final String IPLIST_NAME = "iplist";

    protected static final String IMAGE = "image";

    protected static final String CLUSTER_ID = "clusterId";

    protected static final String USER_GROUP = "userGroup";

    protected static final String PLUGIN_TYPE = "pluginType";

    protected static final String CLUSTER_NAME = "clusterName";

    protected static final String REDIS_PASSWORD = "redisPassword";

    @Resource
    protected ClusterLogic clusterLogic;

    @Resource
    private RedisManager redisManager;

    public boolean installTemplate(PluginParent pluginParent, JSONObject reqParam) {
        logger.info("Request Param: " + reqParam);
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
        // 如果安装成功
        if (checkRes) {
            boolean isExtends = false;
            //如果有传 clusterId 那么证明是从扩容界面来的，如果有密码，则将新增的节点密码设置为同之前一样
            String redisPassword = null;
            if(reqParam.containsKey(REDIS_PASSWORD)){
                redisPassword = reqParam.getString(REDIS_PASSWORD);
            }
            logger.info(redisPassword);
            if (reqParam.containsKey(CLUSTER_ID) && StringUtils.isNotBlank(reqParam.getString(CLUSTER_ID))) {
                clusterId = reqParam.getInt(CLUSTER_ID);
                isExtends = true;
                // 获取之前节点的密码，为了统一
                Cluster cluster = clusterLogic.getCluster(clusterId);
                redisPassword = cluster.getRedisPassword();
            } else {
                clusterId = pluginParent.addCluster(reqParam);
            }

            // 集群已经存在，新增节点
            if (clusterId != -1) {
                pluginParent.addNodeList(reqParam, clusterId);
            }
            pluginParent.buildRedisCluster(clusterId, ipMap,isExtends);

            // 建立集群成功：如果redis需要设置密码，统一auth，默认一套集群对应一个密码
            if(StringUtils.isNotEmpty(redisPassword)){
                auth(reqParam.getString(IPLIST_NAME), redisPassword);
                updateClusterPassword(clusterId, redisPassword);
            }
        }
        return checkRes;
    }

    protected abstract boolean checkInstall(JSONObject reqParam);

    protected abstract void addNodeList(JSONObject reqParam, int clusterId);

    protected abstract void installNodeList(JSONObject reqParam, List<RedisNode> nodelist);

    /**
     * 如果必要，给redis统一添加密码
     * @param ipListStr
     * @return
     */
    protected abstract void auth(String ipListStr , String redisPassword);

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
            cluster.setUserGroup(reqParam.getString(USER_GROUP));
            cluster.setClusterType(reqParam.getString(PLUGIN_TYPE));
            cluster.setClusterName(reqParam.getString(CLUSTER_NAME));
            clusterId = clusterLogic.addCluster(cluster);
        }
        return clusterId;
    }

    protected int updateClusterPassword(int clusterId, String  password) {
        return clusterLogic.updateRedisPassword(clusterId,password);
    }

    protected boolean buildRedisCluster(int clusterId, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
        return redisManager.buildCluster(clusterId, ipMap,isExtends);
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
