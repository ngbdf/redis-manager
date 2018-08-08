package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.app.dao.impl.NodeInfoDao;
import com.newegg.ec.cache.app.model.Cluster;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by gl49 on 2018/4/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterDaoTest {
    @Autowired
    private IClusterDao clusterDao;
    @Resource
    private NodeInfoDao nodeInfoTable;

    @Test
    public void addTest(){
        Cluster cluster = new Cluster();
        cluster.setAddress("localhost:8379");
        cluster.setUserGroup("admin");
        cluster.setClusterType("humpback");
        cluster.setClusterName("redis_3");
        clusterDao.addCluster(cluster);
        nodeInfoTable.createTable("node_info_" + cluster.getId());
    }
    @Test
    public void createNodeInfoTableTest(){
        nodeInfoTable.createTable("node_info_45");
    }

    @Test
    public void removeClaster(){
        int id =  33;
        clusterDao.removeCluster( id );
        nodeInfoTable.dropTable( "node_info_" + id );
    }

    @Test
    public void getClusterTest(){
        Cluster cluster = clusterDao.getCluster(1);
        System.out.println( cluster );
    }

    @Test
    public void getClusterListTest(){
        List<Cluster> clusterList = clusterDao.getClusterList("admin2");
        System.out.println( clusterList );
    }

    @Test
    public void updateCluster(){
        boolean res = clusterDao.updateClusterAddress(1, "localhost:8018");
        System.out.println( res );
    }

    @Test
    public void getClusterGroups(){
        List<String> clusters = clusterDao.getClusterGroups();
        System.out.println( clusters );

    }

}
