package com.newegg.ec.cache.plugin.humpback;

import com.google.common.collect.Lists;
import com.newegg.ec.cache.app.controller.check.CheckLogic;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.util.DateUtil;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.httpclient.HttpClientUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import com.newegg.ec.cache.plugin.INodeOperate;
import com.newegg.ec.cache.plugin.basemodel.Node;
import com.newegg.ec.cache.plugin.basemodel.PluginParent;
import com.newegg.ec.cache.plugin.basemodel.StartType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class HumpbackManager extends PluginParent implements INodeOperate {


    private static final CommonLogger logger = new CommonLogger(HumpbackManager.class);

    private static final String CONTAINER_OPTION_API = "containers";
    private static final String IMAGE_OPTION_API = "images";
    static ExecutorService executorService = Executors.newFixedThreadPool(100);

    @Autowired
    IHumpbackNodeDao nodeDao;
    @Resource
    CheckLogic checkLogic;
    @Value("${cache.humpback.image}")
    private String images;
    @Value("${cache.humpback.api.format}")
    private String humpbackApiFormat;

    public HumpbackManager() {

    }

    /**
     * pull image
     *
     * @param pullParam
     * @return
     */
    @Override
    public boolean pullImage(JSONObject pullParam) {
        boolean res = true;
        String ipStr = pullParam.getString(PluginParent.IPLIST_NAME);
        Set<String> ipSet = JedisUtil.getIPList(ipStr);
        String imageUrl = pullParam.getString(PluginParent.IMAGE);
        logger.websocket("start pull image ");
        List<Future<String>> futureList = new ArrayList<>();
        for (String ip : ipSet) {
            Future<String> future = executorService.submit(new PullImageTask(ip, imageUrl));
            futureList.add(future);
        }
        for (Future<String> future : futureList) {
            try {
                String pullRes = future.get();
                logger.websocket("pull image : " + pullRes);
            } catch (Exception e) {
                res = false;
                logger.websocket(e.getMessage());
                logger.error("", e);
            }
        }
        logger.websocket("pull image is finish");
        return res;
    }

    /**
     * cluster install
     *
     * @param installParam
     * @return
     */
    @Override
    public boolean install(JSONObject installParam) {
        return installTemplate(this, installParam);
    }

    /**
     * node start
     *
     * @param startParam
     * @return
     */
    @Override
    public boolean start(JSONObject startParam) {
        String ip = startParam.getString("ip");
        String containerId = startParam.getString("containerName");
        return optionContainer(ip, containerId, StartType.start);
    }

    /**
     * node stop
     *
     * @param stopParam
     * @return
     */
    @Override
    public boolean stop(JSONObject stopParam) {
        String ip = stopParam.getString("ip");
        String containerId = stopParam.getString("containerName");
        return optionContainer(ip, containerId, StartType.stop);
    }

    /**
     * node restart
     *
     * @param restartParam
     * @return
     */
    @Override
    public boolean restart(JSONObject restartParam) {
        boolean res;
        res = stop(restartParam);
        if (res) {
            res = start(restartParam);
        }
        return res;
    }

    /**
     * node remove
     *
     * @param removePram
     * @return
     */
    @Override
    public boolean remove(JSONObject removePram) {
        logger.websocket(removePram.toString());
        int id = removePram.getInt("id");
        String ip = removePram.getString("ip");
        String containerId = removePram.getString("containerName");
        try {
            String url = getApiAddress(ip) + CONTAINER_OPTION_API;
            if (HttpClientUtil.getDeleteResponse(url, containerId) == null) {
                return false;
            }
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
        nodeDao.removeHumbackNode(id);
        return true;
    }

    /**
     * get image list from .yml
     *
     * @return
     */
    @Override
    public List<String> getImageList() {
        return Lists.newArrayList(images.split(","));
    }

    /**
     * show node list table in wesite
     *
     * @param clusterId
     * @return
     */
    @Override
    public List<Node> getNodeList(int clusterId) {
        List<Node> list = nodeDao.getHumbackNodeList(clusterId);
        return list;
    }

    public String getApiAddress(String ip) {
        return String.format(humpbackApiFormat, ip);
    }

    /**
     * humpback 生成redis节点
     *
     * @param reqParam
     * @param nodelist
     */
    @Override
    protected void installNodeList(JSONObject reqParam, List<RedisNode> nodelist) {
        String image = reqParam.getString(PluginParent.IMAGE);
        String containerName = reqParam.getString("containerName");
        List<Future<Boolean>> futureList = new ArrayList<>();
        nodelist.forEach(node -> {
            String ip = String.valueOf(node.getIp());
            int port = node.getPort();
            String name = formatContainerName(containerName, port);
            String command = ip + " " + port;
            JSONObject reqObject = generateInstallObject(image, name, command);
            futureList.add(executorService.submit(new RedisInstallTask(ip, reqObject)));
        });

        for (Future<Boolean> future : futureList) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        logger.websocket("redis cluster node install success");
    }

    @Override
    protected void auth(String ipListStr, String redisPassword) {
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        nodelist.forEach(node -> {
            clusterLogic.addRedisPassd(node.getIp(), node.getPort(),redisPassword);
        });
    }

    private String formatContainerName(String containerName, int port) {
        return containerName + port;
    }

    @Override
    public String checkAccess(JSONObject reqParam) {
        String ipListStr = reqParam.getString(IPLIST_NAME);
        String containerName = reqParam.getString("containerName");
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        String errorMsg = "";
        for (RedisNode redisNode : nodelist) {
            String ip = redisNode.getIp();
            int port = redisNode.getPort();
            String fomatName = formatContainerName(containerName, port);
            JSONObject res = getContainerInfo(ip, fomatName);
            if (res.containsKey("Code") && res.getInt("Code") <= 200) {
                errorMsg += logger.websocket(ip + ":" + port + " the container name " + fomatName + "alreay exit");
                break;
            }
        }
        return errorMsg;
    }

    /**
     * 生成install node所需要的参数
     *
     * @param image
     * @param name
     * @param command
     * @return
     */
    private JSONObject generateInstallObject(String image, String name, String command) {

        JSONObject reqObject = new JSONObject();
        reqObject.put("Image", image);
        JSONArray volumes = new JSONArray();
        JSONObject volumeObj = new JSONObject();
        volumeObj.put("ContainerVolume", "/data/redis");
        volumeObj.put("HostVolume", "/data/redis");
        volumes.add(volumeObj);
        reqObject.put("Volumes", volumes);
        reqObject.put("NetworkMode", "host");
        reqObject.put("RestartPolicy", "always");
        reqObject.put("CPUShares", 0);
        reqObject.put("Memory", 0);
        reqObject.put("Name", name);
        reqObject.put("Command", command);

        return reqObject;
    }

    @Override
    protected boolean checkInstall(JSONObject reqParam) {
        String res = checkAccess(reqParam);
        if (StringUtils.isBlank(res)) {
            return true;
        }
        return false;
    }

    /**
     * table humpback_node 写入数据
     *
     * @param reqParam
     * @param clusterId
     */
    @Override
    protected void addNodeList(JSONObject reqParam, int clusterId) {
        String ipListStr = reqParam.getString(IPLIST_NAME);
        String image = reqParam.getString(PluginParent.IMAGE);
        String containerName = reqParam.getString("containerName");
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        for (RedisNode redisNode : nodelist) {
            HumpbackNode node = new HumpbackNode();
            node.setClusterId(clusterId);
            node.setContainerName(formatContainerName(containerName, redisNode.getPort()));
            node.setUserGroup(reqParam.get("userGroup").toString());
            node.setImage(image);
            node.setIp(redisNode.getIp());
            node.setPort(redisNode.getPort());
            node.setAddTime(DateUtil.getTime());
            nodeDao.addHumbackNode(node);
        }
    }

    /**
     * container option
     *
     * @param ip
     * @param containerName
     * @param startType
     * @return
     */
    public boolean optionContainer(String ip, String containerName, StartType startType) {
        JSONObject object = new JSONObject();
        object.put("Action", startType);
        object.put("Container", containerName);
        try {
            String url = getApiAddress(ip) + CONTAINER_OPTION_API;
            if (HttpClientUtil.getPutResponse(url, object) == null) {
                return false;
            }
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
        return true;
    }

    /**
     * create  container 创建失败会返回一个空的json串
     *
     * @param ip
     * @param param
     * @return
     */
    public JSONObject createContainer(String ip, JSONObject param) {

        String response = null;
        try {
            String url = getApiAddress(ip) + CONTAINER_OPTION_API;
            response = HttpClientUtil.getPostResponse(url, param);
        } catch (IOException e) {
            logger.error("", e);
        }
        return JSONObject.fromObject(response);
    }


    /**
     * 获取container 信息
     *
     * @param ip
     * @param containerId
     * @return
     */
    public JSONObject getContainerInfo(String ip, String containerId) {
        JSONObject res = null;
        try {
            String url = getApiAddress(ip) + CONTAINER_OPTION_API;
            String response = HttpClientUtil.getGetResponse(url, containerId);
            res = JSONObject.fromObject(response);
        } catch (IOException e) {
            logger.error("", e);
            res = new JSONObject();
        }
        return res;
    }

    /**
     * pull images task
     */
    class PullImageTask implements Callable<String> {
        private String image;
        private String ip;

        public PullImageTask(String ip, String image) {
            this.ip = ip;
            this.image = image;
        }

        @Override
        public String call() throws Exception {
            String res = "";
            try {
                JSONObject reqObject = new JSONObject();
                reqObject.put("Image", this.image);
                String url = getApiAddress(ip) + IMAGE_OPTION_API;
                res = logger.websocket("start pull image " + url);
                HttpClientUtil.getPostResponse(url, reqObject);
            } catch (Exception e) {
                res = logger.websocket(e.getMessage());
            }
            return res;
        }
    }

    /**
     * redis humpback install task
     */
    class RedisInstallTask implements Callable<Boolean> {

        private String ip;
        private JSONObject installObj;

        public RedisInstallTask(String ip, JSONObject installObj) {
            this.ip = ip;
            this.installObj = installObj;
        }

        @Override
        public Boolean call() throws Exception {
            if (createContainer(ip, installObj) != null) {
                return true;
            }
            return false;
        }
    }
}
