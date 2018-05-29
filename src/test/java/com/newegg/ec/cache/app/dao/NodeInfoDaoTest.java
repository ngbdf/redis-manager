package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.app.dao.impl.NodeInfoDao;
import com.newegg.ec.cache.app.model.NodeInfo;
import com.newegg.ec.cache.app.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by lzz on 2018/4/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class NodeInfoDaoTest {
    @Autowired
    private INodeInfoDao nodeInfoDao;
    @Resource
    private NodeInfoDao nodeInfoTable;

    @Test
    public void testCreate(){
        nodeInfoTable.createTable("node_info_5");
    }

    @Test
    public void testAdd(){
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setConnectedClients(1100);
        nodeInfo.setBlockedClients(10100);
        nodeInfo.setAddTime(130);
        nodeInfo.setHost("localhost:8018");
        nodeInfoDao.addNodeInfo("node_info_5", nodeInfo);
    }

    @Test
    public void testGroupNodeInfo(){
        List<NodeInfo> res  = nodeInfoDao.getGroupNodeInfo("hello", 0, DateUtil.getTime(), "all", "sum", "day");
        System.out.println( res );
    }

    @Test
    public void testGetMaxFields(){
        List<Map> res = nodeInfoDao.getMaxField("hello", 0, DateUtil.getTime(), "connected_clients", 2);
        System.out.println( res );
    }

    @Test
    public void testGetMinFields(){
        List<Map> res = nodeInfoDao.getMinField("hello", 0, DateUtil.getTime(), "connected_clients", 2);
        System.out.println( res );
    }

    @Test
    public void testGetAvgField(){
        String res = nodeInfoDao.getAvgField("hello", 0, DateUtil.getTime(), "all", "connected_clients");
        System.out.println( res );
    }

    @Test
    public void testGetAllField(){
        String res = nodeInfoDao.getAllField("hello", 0, DateUtil.getTime(), "connected_clients");
        System.out.println( res );
    }

    @Test
    public void testGetLastField(){
        NodeInfo nodeInfo = nodeInfoDao.getLastNodeInfo("hello", 0, DateUtil.getTime(), "localhost:8008");
        System.out.println( nodeInfo );

    }
}
