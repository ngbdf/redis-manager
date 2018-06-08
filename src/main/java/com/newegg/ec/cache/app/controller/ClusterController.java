package com.newegg.ec.cache.app.controller;

import com.newegg.ec.cache.app.dao.impl.NodeInfoDao;
import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.model.*;
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
    private NodeInfoDao nodeInfoTable;

    @RequestMapping("/clusterListManager")
    public String clusterListManager(Model model){
        return "clusterListManager";
    }

    @RequestMapping("/clusterManager")
    public String clusterManager(Model model){
        return "clusterManager";
    }

    @RequestMapping(value = "/redisQuery", method = RequestMethod.POST)
    @ResponseBody
    public Response redisQuery(@RequestBody RedisQueryParam redisQueryParam){
        Object res = logic.query( redisQueryParam );
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/redisDbList", method = RequestMethod.GET)
    @ResponseBody
    public Response redisDbList(@RequestParam String address){
        List<Map<String, String>> redisDBList = logic.getRedisDBList(address);
        return Response.Result(0, redisDBList);
    }

    @RequestMapping(value = "/listCluster", method = RequestMethod.GET)
    @ResponseBody
    public Response listCluster(@RequestParam String group){
        List<Cluster> listCluster = null;
        listCluster = logic.getClusterList( group );
        return Response.Result(0, listCluster);
    }

    @RequestMapping(value = "/clusterExistAddress", method = RequestMethod.GET)
    @ResponseBody
    public Response clusterExistAddress(@RequestParam String address){
        boolean isexist = logic.clusterExistAddress( address );
        return Response.Result(Response.DEFAULT, isexist);
    }


    @RequestMapping(value = "/listClusterByUser", method = RequestMethod.GET)
    @ResponseBody
    public Response listClusterByUser(@SessionAttribute(Common.SESSION_USER_KEY) User user){
        List<Cluster> listCluster = null;
        listCluster = logic.getClusterListByUser( user );
        return Response.Result(0, listCluster);
    }

    @RequestMapping(value = "/getClusterListInfo", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterListInfo(@SessionAttribute(Common.SESSION_USER_KEY) User user){
        Map<String, Integer> clusterListInfo = null;
        if (user != null) {
            clusterListInfo = logic.getClusterListInfo(user.getUserGroup());
        }
        return Response.Result(0, clusterListInfo);
    }


    @RequestMapping(value = "/getCluster", method = RequestMethod.GET)
    @ResponseBody
    public Response getCluster(@RequestParam int id){
        Cluster cluster = logic.getCluster( id );
        return Response.Result(0, cluster);
    }

    @RequestMapping(value = "/getClusterHost", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterHost(@RequestParam int id){
        Host host = logic.getClusterHost( id );
        return Response.Result(0, host);
    }

    @RequestMapping(value = "/addCluster", method = RequestMethod.POST)
    @ResponseBody
    public Response addCluster(@RequestBody Cluster cluster){
        int res = logic.addCluster( cluster );
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/removeCluster", method = RequestMethod.POST)
    @ResponseBody
    public Response removeCluster(@RequestParam String clusterId){
        int id = Integer.parseInt(clusterId);
        boolean res = logic.removeCluster(id);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getClusterInfo", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterInfo(@RequestParam String ip, @RequestParam int port){
        Map<String, String> res = logic.getClusterInfo(ip, port);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getClusterInfoByAddress", method = RequestMethod.GET)
    @ResponseBody
    public Response getClusterInfoByAddress(@RequestParam String address){
        Map<String, String> res = logic.getClusterInfo(address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getNodeInfo", method = RequestMethod.GET)
    @ResponseBody
    public Response getNodeInfo(@RequestParam String address){
        Map<String, String> res = logic.getNodeInfo(address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/getRedisConfig", method = RequestMethod.GET)
    @ResponseBody
    public Response getRedisConfig(@RequestParam String address){
        Map<String, String> res = logic.getRedisConfig(address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/nodeList", method = RequestMethod.GET)
    @ResponseBody
    public Response nodeList(@RequestParam String address){
        List<Map<String, String>> list = logic.nodeList(address);
        return Response.Result(0, list);
    }

    @RequestMapping(value = "/detailNodeList", method = RequestMethod.GET)
    @ResponseBody
    public Response detailNodeList(@RequestParam String address){
        Map<String, Map> detailNodeList = logic.detailNodeList(address);
        Map<String,Object> res = new HashMap<>();
        res.put("nodeList", detailNodeList);
        Map<String, String> clusterInfo = logic.getClusterInfo(address);
        res.put("clusterInfo", clusterInfo);
        System.out.println(res);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/beSlave", method = RequestMethod.GET)
    @ResponseBody
    public Response beSlave(@RequestParam String ip, @RequestParam int port, @RequestParam String masterId){
        boolean res = logic.beSlave(ip, port, masterId);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/beMaster", method = RequestMethod.GET)
    @ResponseBody
    public Response beMaster(@RequestParam String ip, @RequestParam int port){
        boolean res = logic.beMaster(ip, port);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/initSlot", method = RequestMethod.GET)
    @ResponseBody
    public Response initSlot(@RequestParam String address){
        boolean res = logic.initSlot(address);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/forgetNode", method = RequestMethod.GET)
    @ResponseBody
    public Response forgetNode(@RequestParam String ip, @RequestParam int port, @RequestParam String masterId){
        boolean res = logic.forgetNode(ip, port, masterId);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/moveSlot", method = RequestMethod.GET)
    @ResponseBody
    public Response moveSlot(@RequestParam String ip, @RequestParam int port, @RequestParam int startKey, @RequestParam int endKey){
        boolean res = logic.reShard(ip, port, startKey, endKey);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/importNode", method = RequestMethod.GET)
    @ResponseBody
    public Response importNode(@RequestParam String ip, @RequestParam int port, @RequestParam String masterIP, @RequestParam int masterPort){
        boolean res = logic.importNode(ip, port, masterIP, masterPort);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/batchConfig", method = RequestMethod.GET)
    @ResponseBody
    public Response batchConfig(@RequestParam String ip, @RequestParam int port, @RequestParam String configName, @RequestParam String configValue){
        boolean res = logic.batchConfig(ip, port, configName, configValue);
        if( res ){
            return Response.Info("modify config is sucess");
        }
        return Response.Warn("modify config is fail");
    }
}
