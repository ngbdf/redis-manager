package com.newegg.ec.cache.app.controller;

import com.newegg.ec.cache.app.dao.impl.NodeInfoDao;
import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.logic.ExtensionLogic;
import com.newegg.ec.cache.app.model.*;
import com.newegg.ec.cache.app.util.RequestUtil;
import com.newegg.ec.cache.core.userapi.UserAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/21.
 */
@Controller
@RequestMapping("/cluster")
@UserAccess
public class ClusterController {
    @Autowired
    private ClusterLogic logic;

    @Autowired
    private ExtensionLogic extensionLogic;


    @Autowired
    private NodeInfoDao nodeInfoTable;

    @RequestMapping("/clusterManager")
    public String clusterManager(Model model) {
        return "clusterManager";
    }

    @RequestMapping("/synCluster")
    public String synCluster(Model model) {
        return "synCluster";
    }

    @RequestMapping(value = "/redisQuery", method = RequestMethod.POST)
    @ResponseBody
    public Response redisQuery(@RequestBody RedisQueryParam redisQueryParam) {
        Object res = logic.query(redisQueryParam);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/redisDbList", method = RequestMethod.GET)
    @ResponseBody
    public Response redisDbList(@RequestParam int clusterId, @RequestParam String address) {
        List<Map<String, String>> redisDBList = logic.getRedisDBList(clusterId, address);

        return Response.Result(0, redisDBList);
    }

    @RequestMapping(value = "/clustersGroup", method = RequestMethod.GET)
    @ResponseBody
    public Response clustersGroup() {
        Map<String, List<Cluster>> listCluster = null;
        String userGroup = RequestUtil.getUser().getUserGroup();
        listCluster = logic.getClusterMap(userGroup);
        return Response.Result(0, listCluster);
    }

    @RequestMapping(value = "/listCluster", method = RequestMethod.GET)
    @ResponseBody
    public Response listCluster() {
        String userGroup = RequestUtil.getUser().getUserGroup();
        List<Cluster> listCluster = logic.getClusterList(userGroup);
        return Response.Result(0, listCluster);
    }

    @RequestMapping(value = "/getClusterListByGroup", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterListByGroup(@RequestParam String group) {
        List<Cluster> listCluster = logic.getClusterListByGroup(group);
        return Response.Result(0, listCluster);
    }

    @RequestMapping(value = "/clusterExistAddress", method = RequestMethod.GET)
    @ResponseBody
    public Response clusterExistAddress(@RequestParam String address) {
        boolean isexist = logic.clusterExistAddress(address);
        return Response.Result(Response.DEFAULT, isexist);
    }

    @RequestMapping(value = "/importDataToCluster", method = RequestMethod.GET)
    @ResponseBody
    public Response importDataToCluster(@RequestParam int clusterId, @RequestParam String address, @RequestParam String targetAddress, @RequestParam String keyFormat) {
        boolean res = logic.importDataToCluster(clusterId, address, targetAddress, keyFormat);
        if (res) {
            return Response.Success();
        }
        return Response.Error("import is error!");
    }

    @RequestMapping(value = "/getImportCountList", method = RequestMethod.GET)
    @ResponseBody
    public Response getImportCountList() {
        List<ClusterImportResult> clusterImportResultList = logic.getImportCountList();
        return Response.Result(0, clusterImportResultList);
    }

    @RequestMapping(value = "/listClusterByUser", method = RequestMethod.GET)
    @ResponseBody
    public Response listClusterByUser(@SessionAttribute(Constants.SESSION_USER_KEY) User user) {
        List<Cluster> listCluster = null;
        listCluster = logic.getClusterListByUser(user);
        return Response.Result(0, listCluster);
    }

    @RequestMapping(value = "/getClusterListInfo", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterListInfo(@SessionAttribute(Constants.SESSION_USER_KEY) User user) {
        Map<String, Integer> clusterListInfo = null;
        if (user != null) {
            clusterListInfo = logic.getClusterListInfo(user.getUserGroup());
        }
        return Response.Result(0, clusterListInfo);
    }

    @RequestMapping(value = "/getCluster", method = RequestMethod.GET)
    @ResponseBody
    public Response getCluster(@RequestParam int id) {
        Cluster cluster = logic.getCluster(id);
        return Response.Result(0, cluster);
    }

    @RequestMapping(value = "/getClusterHost", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterHost(@RequestParam int id) {
        Host host = logic.getClusterHost(id);
        return Response.Result(0, host);
    }

    @RequestMapping(value = "/addCluster", method = RequestMethod.POST)
    @ResponseBody
    public Response addCluster(@RequestBody Cluster cluster) {
        int res = logic.addCluster(cluster);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/removeCluster", method = RequestMethod.GET)
    @ResponseBody
    public Response removeCluster(@RequestParam String clusterId) {
        int id = Integer.parseInt(clusterId);
        boolean res = logic.removeCluster(id);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getClusterInfo", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterInfo(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port) {
        Map<String, String> res = logic.getClusterInfo(clusterId, ip, port);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getClusterInfoByAddress", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterInfoByAddress(@RequestParam int clusterId, @RequestParam String address) {
        Map<String, String> res = logic.getClusterInfo(clusterId, address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getNodeInfo", method = RequestMethod.GET)
    @ResponseBody
    public Response getNodeInfo(@RequestParam int clusterId, @RequestParam String address) {
        Map<String, String> res = logic.getNodeInfo(clusterId, address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getRedisConfig", method = RequestMethod.GET)
    @ResponseBody
    public Response getRedisConfig(@RequestParam int clusterId, @RequestParam String address) {
        Map<String, String> res = logic.getRedisConfig(clusterId, address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/nodeList", method = RequestMethod.GET)
    @ResponseBody
    public Response nodeList(@RequestParam int clusterId, @RequestParam String address) {
        List<Map<String, String>> list = logic.nodeList(clusterId, address);
        return Response.Result(0, list);
    }

    @RequestMapping(value = "/detailNodeList", method = RequestMethod.GET)
    @ResponseBody
    public Response detailNodeList(@RequestParam int clusterId, @RequestParam String address) {
        Map<String, Map> detailNodeList = logic.detailNodeList(clusterId, address);
        Map<String, Object> res = new HashMap<>();
        res.put("nodeList", detailNodeList);
        Map<String, String> clusterInfo = logic.getClusterInfo(clusterId, address);
        res.put("clusterInfo", clusterInfo);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/beSlave", method = RequestMethod.GET)
    @ResponseBody
    public Response beSlave(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port, @RequestParam String masterId) {
        boolean res = logic.beSlave(clusterId, ip, port, masterId);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/beMaster", method = RequestMethod.GET)
    @ResponseBody
    public Response beMaster(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port) {
        boolean res = logic.beMaster(clusterId, ip, port);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/initSlot", method = RequestMethod.GET)
    @ResponseBody
    public Response initSlot(@RequestParam int clusterId, @RequestParam String address) {
        boolean res = logic.initSlot(clusterId, address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/forgetNode", method = RequestMethod.GET)
    @ResponseBody
    public Response forgetNode(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port, @RequestParam String masterId) {
        boolean res = logic.forgetNode(clusterId, ip, port, masterId);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/moveSlot", method = RequestMethod.GET)
    @ResponseBody
    public Response moveSlot(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port, @RequestParam int startKey, @RequestParam int endKey) {
        boolean res = logic.reShard(clusterId, ip, port, startKey, endKey);
        if (res) {
            return Response.Success();
        }
        return Response.Error("move slot fail");
    }

    @RequestMapping(value = "/importNode", method = RequestMethod.GET)
    @ResponseBody
    public Response importNode(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port, @RequestParam String masterIP, @RequestParam int masterPort) {
        boolean res = logic.importNode(clusterId, ip, port, masterIP, masterPort);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/batchConfig", method = RequestMethod.GET)
    @ResponseBody
    public Response batchConfig(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port, @RequestParam String configName, @RequestParam String configValue) {
        boolean res = logic.batchConfig(clusterId, ip, port, configName, configValue);
        if (res) {
            return Response.Info("modify config is sucess");
        }
        return Response.Warn("modify config is fail");
    }

    @RequestMapping(value = "/memoryPurge", method = RequestMethod.GET)
    @ResponseBody
    public Response memoryPurge(@RequestParam int clusterId, @RequestParam String ip, @RequestParam int port) {
        String res = logic.memoryPurge(clusterId, ip, port);
        if (res != null) {
            return Response.Info(res);
        }
        return Response.Warn("memory purge is fail");
    }

    @RequestMapping(value = "/memoryDoctor", method = RequestMethod.GET)
    @ResponseBody
    public Response memoryDoctor(@RequestParam int clusterId) {
        List<Map<String, String>> doctorResult = extensionLogic.memoryDoctor(clusterId);
        if (doctorResult != null && doctorResult.size() > 0) {
            return Response.Result(0, doctorResult);
        }
        return Response.Result(0,"This Cluster Can not get effective memory diagnosis result");
    }
}
