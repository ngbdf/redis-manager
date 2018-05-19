package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.component.RedisManager;
import com.newegg.ec.cache.app.dao.IClusterDao;
import com.newegg.ec.cache.app.dao.INodeInfoDao;
import com.newegg.ec.cache.app.model.*;
import com.newegg.ec.cache.app.dao.impl.NodeInfoDao;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import com.newegg.ec.cache.app.util.SlotBalanceUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class ClusterLogic {
    private static CommonLogger logger = new CommonLogger( ClusterLogic.class );

    @Autowired
    private IClusterDao clusterDao;
    @Autowired
    private NodeInfoDao nodeInfoTable;
    @Resource
    private RedisManager redisManager;

    public Cluster getCluster(int id){
        return clusterDao.getCluster( id );
    }

    public Object query(RedisQueryParam redisQueryParam){
        return redisManager.query( redisQueryParam );
    }

    public List<Cluster> getClusterList(String group){
        System.out.println( group + "--------------");
        return clusterDao.getClusterList( group );
    }

    public boolean clusterExistAddress(String address){
        List<Cluster> list = clusterDao.getClusterByAddress(address);
        if( list.isEmpty() ){
            return false;
        }
        return true;
    }

    public boolean removeCluster(int id){
        boolean res = false;
        try {
            clusterDao.removeCluster(id);
            String tableName = Common.NODE_INFO_TABLE_FORMAT + id;
            nodeInfoTable.dropTable( tableName );
            res = true;
        }catch (Exception e){

        }
        return res;
    }

    public Map<String, Integer> getClusterListInfo(String userGroup) {
        Map<String, Integer> clusterListInfo = new HashMap<>();
        int clusterOkNumber = 0;
        int clusterFailNumber = 0;
        if (StringUtils.isNotBlank(userGroup)){
            List<Cluster> clusterList = clusterDao.getClusterList(userGroup);
            if (clusterList != null && clusterList.size() > 0){
                clusterListInfo.put(Common.CLUSTER_NUMBER, clusterList.size());
                for (Cluster cluster : clusterList){
                    if (getClusterState(cluster.getId())) {
                        clusterOkNumber++;
                    } else {
                        clusterFailNumber++;
                    }
                }
                clusterListInfo.put(Common.CLUSTER_OK_NUMBER, clusterOkNumber);
                clusterListInfo.put(Common.CLUSTER_FAIL_NUMBER, clusterFailNumber);
            } else {
                clusterListInfo.put(Common.CLUSTER_NUMBER, 0);
                clusterListInfo.put(Common.CLUSTER_OK_NUMBER, 0);
                clusterListInfo.put(Common.CLUSTER_FAIL_NUMBER, 0);
            }
        }
        return clusterListInfo;
    }

    public int addCluster(Cluster cluster){
        int res = -1;
        try {
            int row = clusterDao.addCluster(cluster);
            if (row > 0){
                cluster.setAddress(cluster.getAddress());
                cluster.setUserGroup(cluster.getUserGroup());
                cluster.setClusterName(cluster.getClusterName());
                nodeInfoTable.createTable(Common.NODE_INFO_TABLE_FORMAT + cluster.getId());
                res = cluster.getId();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    public Map<String, String> getClusterInfo(String ip, int port){
        return redisManager.getClusterInfo(ip, port);
    }

    public Map<String, String> getClusterInfo(String address){
        List<Host> host = NetUtil.getHostByAddress( address );
        return redisManager.getClusterInfo(host.get(0).getIp(), host.get(0).getPort());
    }

    public Map<String, String> getNodeInfo(String address){
        Host host = NetUtil.getHostPassAddress( address );
        return redisManager.getMapInfo(host.getIp(), host.getPort());
    }

    public Map<String, String> getRedisConfig(String address){
        Host host = NetUtil.getHostPassAddress( address );
        return redisManager.getRedisConfig(host.getIp(), host.getPort() );
    }

    public List<Map<String, String>> nodeList(String address){
        Host host = NetUtil.getHostPassAddress( address );
        List<Map<String, String>> list = redisManager.nodeList( host.getIp(), host.getPort() );
        return list;
    }

    public Host getClusterHost(int id) {
        Cluster cluster = getCluster(id);
        String addressStr = cluster.getAddress();
        Host host = NetUtil.getHostPassAddress( addressStr );
        return host;
    }

    public boolean getClusterState(int id){
        boolean res = false;
        try {
            Host host = getClusterHost(id);
            final Map<String, String> clusterInfo = getClusterInfo(host.getIp(), host.getPort());
            System.out.println(clusterInfo);
            String state = clusterInfo.get(Common.CLUSTER_STATE);
            if( "ok".equals(state) ){
                res = true;
            }
        }catch (Exception ignore){
            // ignore
        }
        return  res;
    }

    public Map<String,Map> detailNodeList(String address) {
        Host host = NetUtil.getHostPassAddress( address );
        Map<String, Map> result = redisManager.getClusterNodes( host.getIp(), host.getPort() );
        return result;
    }

    public List<Map<String, String>> getRedisDBList(String address){
        Host host = NetUtil.getHostPassAddress(address);
        return redisManager.getRedisDBList(host.getIp(), host.getPort());
    }

    public List<Cluster> getClusterListByUser(User user) {
        String addressStr = user.getUserGroup();
        String[] addressArr = addressStr.split(",");
        List<Cluster> listCluster = new ArrayList<>();
        for(String adddress : addressArr ){
            listCluster.addAll( getClusterList( adddress ) );
        }
        return listCluster;
    }

    public boolean beSlave(String ip, int port, String masterId) {
        return redisManager.beSlave(ip, port, masterId);
    }

    public boolean beMaster(String ip, int port) {
        return redisManager.beMaster(ip, port);
    }

    public boolean forgetNode(String ip, int port, String nodeId) {
        return redisManager.forget(ip, port, nodeId);
    }

    public boolean importNode(String ip, int port, String masterIP, int masterPort) {
        return redisManager.clusterMeet(ip, port, masterIP, masterPort);
    }

    public boolean batchConfig(String myip, int myPort, String configName, String configValue) {
        boolean res = true;
        List<Map<String, String>> nodeList = JedisUtil.getNodeList(myip, myPort);
        for(Map<String, String> node : nodeList){
            String ip = node.get("ip");
            int port = Integer.parseInt(node.get("port"));
            Jedis jedis = new Jedis( ip, port);
            try {
                List<String> configList = jedis.configGet( configName );
                if( configList.size() != 2 ){
                    break;
                }
                String oldValue = configList.get(1).toString();
                if( !oldValue.equals( configValue ) ){
                    jedis.configSet(configName, configValue);
                    jedis.clusterSaveConfig();
                }
            }catch (Exception e){
                res = false;
                logger.error("", e );
            }finally {
                jedis.close();
            }
        }
        return res;
    }

    public boolean initSlot(String address) {
        boolean res = true;
        Host host = NetUtil.getHost( address );
        String ip = host.getIp();
        int port = host.getPort();
        List<Map<String, String>> masterList = JedisUtil.getNodeList (ip, port, true);
        int masterSize = masterList.size();
        List<SlotBalanceUtil.Shade> balanceSlots = SlotBalanceUtil.balanceSlot( masterSize );
        for( int i = 0; i < balanceSlots.size(); i++ ){
            try {
                SlotBalanceUtil.Shade shade = balanceSlots.get(i);
                int start = shade.getStartSlot();
                int end = shade.getEndSlot();
                Map<String, String> hostMap = masterList.get(i);
                String itemIp = hostMap.get("ip");
                String itemPort = hostMap.get("port");
                int intItemPort = Integer.parseInt(itemPort);
                Jedis jedis = new Jedis( itemIp, intItemPort);
                try {
                    for(int slot = start; slot <= end; slot++ ){
                        try {
                            String resstr = jedis.clusterAddSlots( slot );
                            if( !resstr.equals("OK") ){
                                jedis.clusterAddSlots( slot );
                            }
                        }catch (Exception e){
                            logger.error("", e );
                            res = false;
                        }
                    }
                }catch (Exception e){
                    res = false;
                    logger.error("", e );
                }finally {
                    jedis.close();
                }
            }catch (Exception e){
                res = false;
                logger.error("", e );
            }
        }
        return res;
    }
}