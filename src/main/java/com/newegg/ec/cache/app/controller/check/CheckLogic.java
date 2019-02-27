package com.newegg.ec.cache.app.controller.check;

import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.model.*;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import com.newegg.ec.cache.app.util.RemoteShellUtil;
import com.newegg.ec.cache.app.util.RequestUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by gl49 on 2018/4/22.
 */
@Component
public class CheckLogic {
    public static  final CommonLogger logger = new CommonLogger(CheckLogic.class);
    @Autowired
    private ClusterLogic clusterLogic;

    private String checkLog(String msg) {
        return logger.websocket(msg) + "<br>";
    }

    public int checkRedisVersion(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = clusterLogic.getCluster(clusterId);
        ConnectionParam param = new ConnectionParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        int version = JedisUtil.getRedisVersion(param);
        return version;
    }

    public Response checkPortPass(String ip, int port, boolean isPass) {
        boolean res = NetUtil.checkIpAndPort(ip, port);
        if (isPass) {
            return res ? Response.Success() : Response.Error("port is not pass");
        } else {
            return !res ? Response.Success() : Response.Error("port is already use");
        }
    }

    public Response checkIp(String ip) {
        boolean res = NetUtil.checkIp(ip, 5000);
        return res ? Response.Success() : Response.Error("ip is not access");
    }

    public Response checkAddress(String address) {
        Host host = NetUtil.getHostPassAddress(address);
        if (null != host) {
            return Response.Success();
        } else {
            return Response.Error("address is not access");
        }
    }

    public Response checkClusterNameByUserid(String clusterId) {
        User user = RequestUtil.getUser();
        List<Cluster> clusters = clusterLogic.getClusterListByUser(user);
        for (Cluster cluster : clusters) {
            if (clusterId.equals(cluster.getClusterName())) {
                return Response.Error("the cluster name is alreay");
            }
        }
        return Response.Success();
    }

    public Response checkBatchHostNotPass(JSONObject req) {
        String iplist = req.getString("iplist");
        String errorMsg = "";
        String[] ipArr = iplist.split("\n");
        for (String line : ipArr) {
            try {
                checkLog("start check " + line);
                String[] tmpArr = line.split(":");
                if (tmpArr.length >= 2) {
                    String ip = tmpArr[0].trim();
                    if (!NetUtil.checkIp(ip, 5000)) {
                        errorMsg += checkLog(ip + " is not pass");
                        continue;
                    }
                    int port = Integer.parseInt(tmpArr[1].trim());
                    if (!NetUtil.checkIpAndPort(ip, port)) {
                        checkLog(line + " is ok");
                    } else {
                        errorMsg += checkLog(line + " the port is alreay use");
                    }
                } else {
                    errorMsg += checkLog(line + " is format error");
                }
            } catch (Exception e) {
                checkLog(e.getMessage());
            }
        }
        if (!StringUtils.isBlank(errorMsg)) {
            return Response.Error(errorMsg);
        } else {
            return Response.Success();
        }
    }

    public Response checkDockerBatchInstall(JSONObject req) {
        return Response.Success();
    }

    public Response checkMachineBatchInstall(JSONObject req) {
        Response response;
        response = checkBatchHostNotPass(req);
        if (response.getCode() > 0) {
            logger.websocket(response.getMsg());
            return response;
        }
        response = checkBatchDirPermission(req);
        if (response.getCode() > 0) {
            logger.websocket(response.getMsg());
            return response;
        }
        response = checkBatchUserPermisson(req);
        if (response.getCode() > 0) {
            logger.websocket(response.getMsg());
            return response;
        }
        response = checkBatchWgetPermission(req);
        if (response.getCode() > 0) {
            logger.websocket(response.getMsg());
            return response;
        }
        return response;
    }

    public Response checkBatchDirPermission(JSONObject req) {
        String iplist = req.getString("iplist");
        Set<String> ipSet = JedisUtil.getIPList(iplist);
        String errorMsg = "";
        for (String ip : ipSet) {
            String username = req.getString("username");
            String password = req.getString("password");
            String installPath = req.getString("installPath");
            String cmd = getCheckDirPermission(installPath);
            RemoteShellUtil remoteShellUtil = new RemoteShellUtil(ip, username, password);
            errorMsg += logger.websocket(remoteShellUtil.exec(cmd));
        }
        if (!StringUtils.isBlank(errorMsg)) {
            logger.websocket(errorMsg);
            return Response.Error(errorMsg);
        }
        return Response.Success();
    }

    public Response checkBatchWgetPermission(JSONObject req) {
        String iplist = req.getString("iplist");
        Set<String> ipSet = JedisUtil.getIPList(iplist);
        String errorMsg = "";
        for (String ip : ipSet) {
            String username = req.getString("username");
            String password = req.getString("password");
            String cmd = getCheckCommand("wget");
            RemoteShellUtil remoteShellUtil = new RemoteShellUtil(ip, username, password);
            errorMsg += logger.websocket(remoteShellUtil.exec(cmd));
        }
        if (!StringUtils.isBlank(errorMsg)) {
            logger.websocket(errorMsg);
            return Response.Error(errorMsg);
        }
        return Response.Success();
    }

    public Response checkUserPermisson(String ip, String username, String password) {
        boolean res = NetUtil.checkIpAnduserAccess(ip, username, password);
        if (res) {
            return Response.Success();
        }
        return Response.Error("username or password is error");
    }

    public Response checkBatchUserPermisson(JSONObject req) {
        String iplist = req.getString("iplist");
        Set<String> ipSet = JedisUtil.getIPList(iplist);
        String errorMsg = "";
        for (String ip : ipSet) {
            String username = req.getString("username");
            String password = req.getString("password");
            boolean res = NetUtil.checkIpAnduserAccess(ip, username, password);
            if (!res) {
                errorMsg += logger.websocket(username + " user is not permisson <br/>");
            }
        }
        if (!StringUtils.isBlank(errorMsg)) {
            logger.websocket(errorMsg);
            return Response.Error(errorMsg);
        }
        return Response.Success();
    }

    public String getCheckDirPermission(String installPath) {
        return "if [ ! -w '" + installPath + "' ];then echo '" + installPath + " without permission <br>'; fi";
    }

    public String getCheckDirExist(String installPath) {
        return "if [ ! -d '" + installPath + "' ]; then echo 'without the " + installPath + " dir <br>'; fi";
    }

    public String getCheckCommand(String command) {
        return "if [ ! `command -v " + command + "` ]; then echo 'no exists " + command + "<br>'; fi";
    }
}
