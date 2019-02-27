package com.newegg.ec.cache.module.clusterbuild.build;

import com.newegg.ec.cache.core.entity.constants.Constants;
import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.ClusterImportResult;
import com.newegg.ec.cache.core.entity.model.Host;
import com.newegg.ec.cache.core.entity.model.User;
import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.core.entity.redis.RedisQueryParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;
import com.newegg.ec.cache.dao.IClusterDao;
import com.newegg.ec.cache.dao.NodeInfoDao;
import com.newegg.ec.cache.dao.plugin.IDockerNodeDao;
import com.newegg.ec.cache.dao.plugin.IHumpbackNodeDao;
import com.newegg.ec.cache.dao.plugin.IMachineNodeDao;
import com.newegg.ec.cache.module.clusterbuild.common.ClusterManager;
import com.newegg.ec.cache.module.extend.ExtensionService;
import com.newegg.ec.cache.util.NetUtil;
import com.newegg.ec.cache.util.redis.RedisUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class ClusterService extends ExtensionService implements IClusterQueryService,IClusterBuildService{

    private static final  Log logger = LogFactory.getLog(ClusterService.class);

    @Value("${cache.redis.client}")
    private String redisClient;

    @Autowired
    private IClusterDao clusterDao;
    @Autowired
    private NodeInfoDao nodeInfoTable;
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private IMachineNodeDao machineNodeDao;
    @Autowired
    private IDockerNodeDao dockerNodeDao;
    @Autowired
    private IHumpbackNodeDao humpbackNodeDao;

    @Override
    public Cluster getCluster(int id) {

        Cluster cluster = clusterDao.getCluster(id);
        Host host = NetUtil.getHostPassAddress(cluster.getAddress());
        if(host != null){
            RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
            Map<String, String> nodeInfo = RedisUtils.getInfo(param);
            String redisVersion = nodeInfo.get("redis_version");
            if( Integer.valueOf(redisVersion.substring(0, 1)) >= 4){
                cluster.setIsVersion4(true);
            }
        }
        return cluster;
    }

    @Override
    public Map<String, List<Cluster>> getClusterMap(String group) {
        Map<String, List<Cluster>> clusterMap = new LinkedHashMap<>();
        if (group.equals(Constants.ADMIN_GROUP)) {
            List<String> groups = clusterDao.getClusterGroups();
            Collections.sort(groups);
            for (String groupStr : groups) {
                clusterMap.put(groupStr, clusterDao.getClusterList(groupStr));
            }
        }
        clusterMap.put(group, clusterDao.getClusterList(group));
        return clusterMap;
    }

    @Override
    public boolean clusterExistAddress(String address) {
        List<Cluster> list = clusterDao.getClusterByAddress(address);
        if (list.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean removeCluster(int clusterId) {
        boolean res = false;
        try {
            String type = clusterDao.getCluster(clusterId).getClusterType();
            clusterDao.removeCluster(clusterId);
            String tableName = Constants.NODE_INFO_TABLE_FORMAT + clusterId;
            nodeInfoTable.dropTable(tableName);
            switch (type) {
                case "machine":
                    machineNodeDao.removeMachineNodeByClusterId(clusterId);
                    break;
                case "docker":
                    dockerNodeDao.removeDockerNodeByClusterId(clusterId);
                    break;
                case "humpback":
                    humpbackNodeDao.removeHumbackNodeByClusterId(clusterId);
                    break;
            }
            res = true;
        } catch (Exception e) {

        }
        return res;
    }

    @Override
    public Map<String, Integer> getClusterListInfo(String userGroup) {
        Map<String, Integer> clusterListInfo = new HashMap<>();
        int clusterOkNumber = 0;
        int clusterFailNumber = 0;
        if (StringUtils.isNotBlank(userGroup)) {
            if (userGroup.equalsIgnoreCase(Constants.ADMIN_GROUP)) {
                userGroup = null;
            }
            List<Cluster> clusterList = clusterDao.getClusterList(userGroup);
            if (clusterList != null && clusterList.size() > 0) {
                clusterListInfo.put(Constants.CLUSTER_NUMBER, clusterList.size());
                for (Cluster cluster : clusterList) {
                    if (getClusterState(cluster.getId())) {
                        clusterOkNumber++;
                    } else {
                        clusterFailNumber++;
                    }
                }
                clusterListInfo.put(Constants.CLUSTER_OK_NUMBER, clusterOkNumber);
                clusterListInfo.put(Constants.CLUSTER_FAIL_NUMBER, clusterFailNumber);
            } else {
                clusterListInfo.put(Constants.CLUSTER_NUMBER, 0);
                clusterListInfo.put(Constants.CLUSTER_OK_NUMBER, 0);
                clusterListInfo.put(Constants.CLUSTER_FAIL_NUMBER, 0);
            }
        }
        return clusterListInfo;
    }

    @Override
    public int addCluster(Cluster cluster) {
        int res = -1;
        try {
            int row = clusterDao.addCluster(cluster);
            if (row > 0) {
                cluster.setAddress(cluster.getAddress());
                cluster.setUserGroup(cluster.getUserGroup());
                cluster.setClusterName(cluster.getClusterName());
                nodeInfoTable.createTable(Constants.NODE_INFO_TABLE_FORMAT + cluster.getId());
                res = cluster.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<Cluster> getClusterListByUser(User user) {
        List<Cluster> listCluster = getClusterList(user.getUserGroup());
        return listCluster;
    }

    @Override
    public List<Cluster> getClusterList(String group) {
        List<Cluster> clusterList = new ArrayList<>();
        if (group.equals(Constants.ADMIN_GROUP)) {
            List<String> groups = clusterDao.getClusterGroups();
            for (String groupStr : groups) {
                clusterList.addAll(clusterDao.getClusterList(groupStr));
            }
        } else {
            clusterList.addAll(clusterDao.getClusterList(group));
        }
        return clusterList;
    }

    @Override
    public int updateRedisPassword(int id, String redisPassword) {
        return clusterDao.updatePassword(id, redisPassword);
    }

//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public RedisValue query(RedisQueryParam redisQueryParam) {
        return clusterManager.query(redisQueryParam, getCluster(redisQueryParam.getClusterId()));
    }


    public Map<String, String> getClusterInfo(int clusterId, String ip, int port) {
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(ip, port, cluster.getRedisPassword());
        return clusterManager.getClusterInfo(param);
    }

    public Map<String, String> getClusterInfo(int clusterId, String address) {
        List<Host> host = NetUtil.getHostByAddress(address);
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.get(0).getIp(), host.get(0).getPort(), cluster.getRedisPassword());
        return clusterManager.getClusterInfo(param);
    }

    public Map<String, String> getNodeInfo(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        return clusterManager.getMapInfo(param);
    }

    public Map<String, String> getRedisConfig(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        return clusterManager.getRedisConfig(param);
    }

    public List<Map<String, String>> nodeList(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        List<Map<String, String>> list = clusterManager.nodeList(param);
        return list;
    }

    public Host getClusterHost(int id) {
        Cluster cluster = getCluster(id);
        String addressStr = cluster.getAddress();
        Host host = NetUtil.getHostPassAddress(addressStr);
        return host;
    }

    public boolean getClusterState(int clusterId) {
        boolean res = false;
        try {
            Host host = getClusterHost(clusterId);
            final Map<String, String> clusterInfo = getClusterInfo(clusterId, host.getIp(), host.getPort());
            String state = clusterInfo.get(Constants.CLUSTER_STATE);
            if ("ok".equals(state)) {
                res = true;
            }
        } catch (Exception ignore) {
            // ignore
        }
        return res;
    }

    public Map<String, Map> detailNodeList(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        Map<String, Map> result = clusterManager.getClusterNodes(param);
        return result;
    }

    public List<Map<String, String>> getRedisDBList(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        return clusterManager.getRedisDBList(param);
    }


    public List<Cluster> getClusterListByGroup(String group) {
        List<Cluster> clusterList = new ArrayList<>();
        clusterList.addAll(clusterDao.getClusterList(group));
        return clusterList;
    }

    public boolean beSlave(int clusterId, String ip, int port, String masterId) {
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(ip, port, cluster.getRedisPassword());
        return clusterManager.beSlave(param, masterId);
    }

    public boolean beMaster(int clusterId, String ip, int port) {
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(ip, port, cluster.getRedisPassword());
        return clusterManager.beMaster(param);
    }

    public boolean forgetNode(int clusterId, String ip, int port, String nodeId) {
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(ip, port, cluster.getRedisPassword());
        return clusterManager.forget(param, nodeId);
    }

    public boolean importNode(int clusterId, String ip, int port, String masterIP, int masterPort) {
        Cluster cluster = getCluster(clusterId);
        RedisConnectParam slaveParam = new RedisConnectParam(ip, port, cluster.getRedisPassword());
        return clusterManager.clusterMeet(slaveParam, masterIP, masterPort);
    }

    public boolean batchConfig(int clusterId, String myIp, int myPort, String configName, String configValue) {
        return clusterManager.batchConfig(getCluster(clusterId),myIp,myPort,configName,configValue);
    }

    public void addRedisPassd(String ip, int port, String password) {
        clusterManager.addRedisPassd(ip,port,password);
    }

    public boolean initSlot(int clusterId, String address) {
        return clusterManager.initSlot(getCluster(clusterId),address);
    }

    public boolean reShard(int clusterId, String ip, int port, int startKey, int endKey) {
        return clusterManager.reShard(getCluster(clusterId),ip,port,startKey,endKey);
    }

    public boolean importDataToCluster(int clusterId, String address, String targetAddress, String keyFormat) {
        boolean res = false;
        try {
            clusterManager.importDataToCluster(getCluster(clusterId), address, targetAddress, keyFormat);
            res = true;
        } catch (Exception e) {
            logger.error("import to cluster", e);
        }
        return res;
    }

    public List<ClusterImportResult> getImportCountList() {
        return clusterManager.getClusterImportResult();
    }

}