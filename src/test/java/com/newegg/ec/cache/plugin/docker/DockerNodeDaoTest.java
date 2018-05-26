package com.newegg.ec.cache.plugin.docker;

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
public class DockerNodeDaoTest {
    
    @Resource
    private IDockerNodeDao dockerNodeDao;

    @Test
    public void testHumbackNodeList(){
        int id = 1;
        List<Node> list = dockerNodeDao.getDockerNodeList(id);
        for(Node node :list){
            DockerNode temp =   (DockerNode)node;
            System.out.println(temp.getClusterId());
        }
        System.out.println( list );
    }

    @Test
    public void testAddHumbackNode(){
        DockerNode dockerNode = new DockerNode();
        dockerNode.setClusterId(1);
        dockerNode.setContainerName("redis8008");
        dockerNode.setUserGroup("admin");
        dockerNode.setImage("redis3.0.6:v3");
        dockerNode.setIp("localhost");
        dockerNode.setPort(8008);
        System.out.println(dockerNodeDao.addDockerNode(dockerNode));
    }

    @Test
    public void testRemovedockerNode(){
        int id = 2;
        System.out.println(dockerNodeDao.removeDockerNode(id));
    }

    @Test
    public void testGetCluster(){
        DockerNode dockerNode = dockerNodeDao.getDockerNode(1);
        System.out.println( dockerNode );
    }

    @Test
    public void testPackageName(){
        String packageName = "redis3.0.5.tar";
        String tmp[] = packageName.split(".tar");
        System.out.println( tmp[0] );
    }
}
