package com.newegg.ec.cache.module.clusterbuild.build;

import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.User;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2019/2/26.
 *
 * <>p</>Cluster 模块对应数据源操作（CRUD）接口
 */
public interface IClusterQueryService {

    /**
     * et Cluster by id
     * @param id
     * @return
     */
    public Cluster getCluster(int id);

    /**
     * get Clusters by group
     * @param group
     * @return
     */
    public Map<String, List<Cluster>> getClusterMap(String group);

    /**
     * cluster address exist in db
     * @param address
     * @return
     */
    public boolean clusterExistAddress(String address);

    /**
     * remove Cluster
     * @param clusterId
     * @return
     */
    public boolean removeCluster(int clusterId);

    /**
     * get Clusters by group
     * @param userGroup
     * @return
     */
    public Map<String, Integer> getClusterListInfo(String userGroup);

    /**
     * add Cluster
     * @param cluster
     * @return
     */
    public int addCluster(Cluster cluster);

    /**
     * get Clusters By User
     * @param user
     * @return
     */
    public List<Cluster> getClusterListByUser(User user);

    /**
     * get Clusters
     * @param group
     * @return
     */
    public List<Cluster> getClusterList(String group);

    /**
     * update cluster password
     * @param id
     * @param redisPassword
     * @return
     */
    public int updateRedisPassword(int id, String redisPassword);


}
