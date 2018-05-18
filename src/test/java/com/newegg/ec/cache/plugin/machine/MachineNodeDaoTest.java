package com.newegg.ec.cache.plugin.machine;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.plugin.basemodel.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by gl49 on 2018/5/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class MachineNodeDaoTest {
    
    @Resource
    private IMachineNodeDao machineNodeDao;

    @Test
    public void testHumbackNodeList(){
        int cluster_id = 3;
        List<Node> list = machineNodeDao.getMachineNodeList(cluster_id);
        for(Node node :list){
            MachineNode temp = (MachineNode)node;
            System.out.println(temp.getUsername());

        }
        System.out.println( list );
    }

    @Test
    public void testAddHumbackNode(){
        MachineNode machineNode = new MachineNode();
        machineNode.setClusterId(2);
        machineNode.setImage("redis3.0.6.tar.gz");
        machineNode.setUsername("lf52");
        machineNode.setPassword("12345678");
        machineNode.setUserGroup("admin");
        machineNode.setIp("10.16.46.172");
        machineNode.setPort(8008);
        machineNode.setInstallPath("/opt/app/redis");
        System.out.println(machineNodeDao.addMachineNode(machineNode));
    }

    @Test
    public void testRemovedockerNode(){
        int id = 1;
        System.out.println(machineNodeDao.removeMachineNode(id));
    }

    @Test
    public void testGetCluster(){
        MachineNode machineNode = machineNodeDao.getMachineNode(2);
        System.out.println( machineNode );
    }
}
