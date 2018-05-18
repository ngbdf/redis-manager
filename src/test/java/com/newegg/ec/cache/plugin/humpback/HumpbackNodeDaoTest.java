package com.newegg.ec.cache.plugin.humpback;

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
public class HumpbackNodeDaoTest {
    @Resource
    private IHumpbackNodeDao humpbackNodeDao;

    @Test
    public void testHumbackNodeList(){
        int id = 1;
        List<Node> list = humpbackNodeDao.getHumbackNodeList(id);
        for(Node node :list){
            HumpbackNode temp =   (HumpbackNode)node;
            System.out.println(temp.getClusterId());
        }
        System.out.println( list );
    }

    @Test
    public void testAddHumbackNode(){
        HumpbackNode humpbackNode = new HumpbackNode();
        humpbackNode.setClusterId(1);
        humpbackNode.setContainerName("redis8018");
        humpbackNode.setUserGroup("admin");
        humpbackNode.setImage("redis3.0.6:v3");
        humpbackNode.setIp("10.16.46.192");
        humpbackNode.setPort(8018);
        System.out.println(humpbackNodeDao.addHumbackNode(humpbackNode));
    }

    @Test
    public void testRemoveHumpbackNode(){
        int id = 6;
        System.out.println(humpbackNodeDao.removeHumbackNode( id ));
    }

    @Test
    public void testGetCluster(){
        HumpbackNode humpbackNode = humpbackNodeDao.getHumpbackNode( 3 );
        System.out.println( humpbackNode );
    }
}
