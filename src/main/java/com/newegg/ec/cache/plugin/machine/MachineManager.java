package com.newegg.ec.cache.plugin.machine;

import com.google.common.collect.Lists;
import com.newegg.ec.cache.app.controller.check.CheckLogic;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.Constants;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.model.Response;
import com.newegg.ec.cache.app.util.DateUtil;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import com.newegg.ec.cache.app.util.RemoteShellUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import com.newegg.ec.cache.plugin.INodeOperate;
import com.newegg.ec.cache.plugin.basemodel.Node;
import com.newegg.ec.cache.plugin.basemodel.PluginParent;
import com.newegg.ec.cache.plugin.basemodel.StartType;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class MachineManager extends PluginParent implements INodeOperate {
    private static final CommonLogger logger = new CommonLogger(MachineManager.class);

    private static String REDIS_INSTALL_FILE = "redis_install.sh";
    @Autowired
    IMachineNodeDao machineNodeDao;
    @Resource
    CheckLogic checkLogic;
    @Value("${server.port}")
    private int servicePort;
    @Value("${cache.machine.image}")
    private String images;
    @Value("${cache.machine.install.shell}")
    private String installShell;
    @Value("${cache.machine.install.package}")
    private String installPackage;
    @Value("${cache.machine.install.basepath}")
    private String installBasePath;

    public MachineManager() {
        //ignore
    }

    @Override
    public String checkAccess(JSONObject reqParam) {
        return null;
    }

    /**
     * 通过检查权限判断是否能拉取镜像
     *
     * @param pullParam
     * @return
     */
    @Override
    public boolean pullImage(JSONObject pullParam) {
        return true;
    }

    @Override
    public boolean install(JSONObject installParam) {
        return installTemplate(this, installParam);
    }

    @Override
    public boolean start(JSONObject startParam) {
        operateNode(startParam, StartType.start);
        return true;
    }

    @Override
    public boolean stop(JSONObject stopParam) {
        operateNode(stopParam, StartType.stop);
        return true;
    }

    @Override
    public boolean restart(JSONObject restartParam) {
        stop(restartParam);
        start(restartParam);
        return true;
    }

    @Override
    public boolean remove(JSONObject removePram) {
        MachineNode machineNode = (MachineNode) JSONObject.toBean(removePram, MachineNode.class);
        int port = machineNode.getPort();
        RemoteShellUtil rms = new RemoteShellUtil(machineNode.getIp(), machineNode.getUsername(), machineNode.getPassword());
        String path = getPortPath(machineNode.getInstallPath(), port);
        if (path.length() > 4) {
            String cmd = "rm -rf " + getPortPath(machineNode.getInstallPath(), port);
            rms.exec(cmd);
        }
        boolean res = machineNodeDao.removeMachineNode(machineNode.getId());
        return res;
    }


    @Override
    public List<String> getImageList() {
        return Lists.newArrayList(images.split(","));
    }

    @Override
    public List<Node> getNodeList(int clusterId) {
        return machineNodeDao.getMachineNodeList(clusterId);
    }

    @Override
    protected boolean checkInstall(JSONObject reqParam) {
        Response checkRes = checkLogic.checkMachineBatchInstall(reqParam);
        if (checkRes.getCode() == Response.DEFAULT) {
            return true;
        }
        return false;
    }

    @Override
    protected void addNodeList(JSONObject reqParam, int clusterId) {
        logger.websocket("start add node to db ...");
        String ipListStr = reqParam.getString(IPLIST_NAME);
        String image = reqParam.getString(PluginParent.IMAGE);
        String username = reqParam.getString("username");
        String password = reqParam.getString("password");
        String installPath = reqParam.getString("installPath");
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        for (RedisNode redisNode : nodelist) {
            MachineNode node = new MachineNode();
            node.setClusterId(clusterId);
            node.setPassword(password);
            node.setUsername(username);
            node.setUserGroup(reqParam.get("userGroup").toString());
            node.setImage(image);
            node.setInstallPath(installPath);
            node.setIp(redisNode.getIp());
            node.setPort(redisNode.getPort());
            node.setAddTime(DateUtil.getTime());
            machineNodeDao.addMachineNode(node);
        }
        logger.websocket("start add node finish!");
    }

    @Override
    protected void installNodeList(JSONObject reqParam, List<RedisNode> nodelist) {
        String installPath = reqParam.getString("installPath").trim();
        String username = reqParam.getString("username").trim();
        String password = reqParam.getString("password").trim();
        String imagePackage = reqParam.getString(PluginParent.IMAGE).trim();
        String localIp = null;
        try {
            localIp = NetUtil.getLocalIp();
        } catch (UnknownHostException e) {
            return;
        }
        String serviceUrl = "http://" + localIp + ":" + servicePort + "/";
        String packageUrl = serviceUrl + installPackage;
        for (RedisNode redisNode : nodelist) {
            try {
                String ip = redisNode.getIp();
                int port = redisNode.getPort();
                RemoteShellUtil rms = new RemoteShellUtil(ip, username, password);
                String[] cmds = new String[3];
                cmds[0] = "cd " + installPath;
                cmds[1] = "/usr/bin/wget http://" + localIp + ":" + servicePort + installShell + " -O " + REDIS_INSTALL_FILE;
                cmds[2] = "bash " + REDIS_INSTALL_FILE + " " + packageUrl + " " + imagePackage + " " + port + " " + installBasePath;
                String cmd = StringUtils.join(cmds, ";");
                String installRes;
                if (ip.equals(localIp)) { // 如果是要安装到本地机器就不要调用ssh api
                    installRes = RemoteShellUtil.localExec(cmd);
                } else {
                    installRes = rms.exec(cmd);
                }
                logger.websocket(installRes);
            } catch (Exception e) {
                logger.websocket(e.getMessage());
            }
        }
    }

    @Override
    protected void auth(String ipListStr, String redisPassword) {
        List<RedisNode> nodelist = JedisUtil.getInstallNodeList(ipListStr);
        nodelist.forEach(node -> {
            clusterLogic.addRedisPassd(node.getIp(), node.getPort(),redisPassword);
        });
    }

    private void operateNode(JSONObject startParam, StartType startType) {

        MachineNode machineNode = (MachineNode) JSONObject.toBean(startParam, MachineNode.class);
        int port = machineNode.getPort();
        logger.info(machineNode.getIp() + " -- " + machineNode.getUsername() + " --- " + machineNode.getPassword());
        RemoteShellUtil rms = new RemoteShellUtil(machineNode.getIp(), machineNode.getUsername(), machineNode.getPassword());
        String cmd = "cd " + getPortPath(machineNode.getInstallPath(), port);
        cmd += ";bash " + startType + ".sh " + port;

        // stop redis in machine with redis password
        int clusterId = startParam.getInt(Constants.CLUSTER_ID);
        Cluster cluster = clusterLogic.getCluster(clusterId);
        String redisPassword = cluster.getRedisPassword();
        if (StringUtils.isNotBlank(redisPassword)) {
            cmd += " " + redisPassword;
        }
        rms.exec(cmd);
    }

    private String getPortPath(String installPath, int port) {
        if (installPath.endsWith("/")) {
            installPath = installPath.substring(0, installPath.length() - 1);
        }
        return installPath + "/" + installBasePath + port + "/";
    }
}
