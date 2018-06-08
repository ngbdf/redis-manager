package com.newegg.ec.cache.plugin.docker;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.plugin.basemodel.StartType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Created by lf52 on 2018/5/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class DockerOptionTest {

    @Autowired
    private DockerManager docker;

    @Test
    public void testgetContainerInfo() {
        System.out.println(docker.getContainerInfo("localhost", "aaaaa_8512"));
    }

    @Test
    public void testcreateContainer() {
        JSONObject installObject = generateInstallObject("ssecbigdata02:5000/redis4.0.1","leoredistest","/redis/redis-4.0.1/start.sh 7001 localhost");
        System.out.println(docker.createContainer("localhost", installObject));
    }
    @Test
    public void testoptionContainer() {
        System.out.println(docker.optionContainer("localhost", "leoredistest", StartType.stop));
    }

    @Test
    public void testremoveContainer() {
        System.out.println(docker.deleteContainer("localhost", "leoredistest"));
    }

    @Test
    public void testimagePull() throws IOException {
        System.out.println(docker.imagePull("localhost", "shec/itemserviceprd:v1.2.3.4.2"));
    }

    private JSONObject generateInstallObject(String image, String name, String command){

        JSONObject req = new JSONObject();
        req.put("Image", image);
        req.put("HostName", name);
        JSONObject hostConfig = new JSONObject();
        hostConfig.put("NetworkMode", "host");
        JSONObject restartPolicy = new JSONObject();
        restartPolicy.put("Name", "always");
        hostConfig.put("RestartPolicy", restartPolicy);
        JSONArray binds = new JSONArray();
        String bindStr = "/data/redis:/data/redis" ;
        binds.add( bindStr );
        hostConfig.put("Binds", binds);
        req.put("HostConfig", hostConfig);
        String[] cmds = command.split("\\s+");
        req.put("Cmd", cmds);

        return req;
    }

}
