package com.newegg.ec.cache.app.controller;

import com.newegg.ec.cache.app.component.NodeManager;
import com.newegg.ec.cache.app.model.Constants;
import com.newegg.ec.cache.app.model.Response;
import com.newegg.ec.cache.app.model.User;
import com.newegg.ec.cache.core.userapi.UserAccess;
import com.newegg.ec.cache.plugin.INodeOperate;
import com.newegg.ec.cache.plugin.basemodel.Node;
import com.newegg.ec.cache.plugin.basemodel.NodeRequestPram;
import com.newegg.ec.cache.plugin.basemodel.PluginType;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lzz on 2018/4/20.
 */
@Controller
@RequestMapping("/node")
@UserAccess
public class NodeController {

    private static final Log logger = LogFactory.getLog(NodeController.class);

    private INodeOperate nodeOperate;

    @Resource
    private NodeManager nodeManager;

    @RequestMapping("/selectClusterType")
    public String selectType(Model model) {
        return "selectClusterType";
    }

    @RequestMapping("/install")
    public String cluster(Model model, @SessionAttribute(Constants.SESSION_USER_KEY) User user) {
        model.addAttribute("user", user);
        return "installNode";
    }

    @RequestMapping("/manager")
    public String manager(Model model, @SessionAttribute(Constants.SESSION_USER_KEY) User user) {
        model.addAttribute("user", user);
        return "nodeManager";
    }

    @RequestMapping(value = "/getPluginList", method = RequestMethod.GET)
    @ResponseBody
    public Response getPluginList() {
        String[] pluginList = nodeManager.pluginList();
        return Response.Result(Response.DEFAULT, pluginList);
    }

    @RequestMapping(value = "/getImageList", method = RequestMethod.GET)
    @ResponseBody
    public Response getImageList(@RequestParam PluginType pluginType) {
        nodeOperate = nodeManager.factoryOperate(pluginType);
        List<String> imageList = nodeOperate.getImageList();
        return Response.Result(Response.DEFAULT, imageList);
    }

    @RequestMapping(value = "/getNodeList", method = RequestMethod.GET)
    @ResponseBody
    public Response getNodeList(@RequestParam PluginType pluginType, @RequestParam int clusterId) {
        nodeOperate = nodeManager.factoryOperate(pluginType);
        List<Node> nodeList = nodeOperate.getNodeList(clusterId);
        return Response.Result(Response.DEFAULT, nodeList);
    }

    @RequestMapping(value = "/getNodeByClusterId", method = RequestMethod.GET)
    @ResponseBody
    public Response getNodeByClusterId(@RequestParam PluginType pluginType, @RequestParam int clusterId) {
        nodeOperate = nodeManager.factoryOperate(pluginType);
        Node node = null;
        try {
            List<Node> nodeList = nodeOperate.getNodeList(clusterId);
            node = nodeList.get(0);
        } catch (Exception ignore) {

        }
        return Response.Result(Response.DEFAULT, node);
    }

    @RequestMapping(value = "/nodeStart", method = RequestMethod.POST)
    @ResponseBody
    public Response nodeStart(@RequestBody NodeRequestPram nodeRequestPram) {
        nodeOperate = nodeManager.factoryOperate(nodeRequestPram.getPluginType());
        boolean res = nodeOperate.start(nodeRequestPram.getReq());
        if (res) {
            return Response.Success();
        }
        return Response.Warn("start fail");
    }

    @RequestMapping(value = "/nodeStop", method = RequestMethod.POST)
    @ResponseBody
    public Response nodeStop(@RequestBody NodeRequestPram nodeRequestPram) {
        nodeOperate = nodeManager.factoryOperate(nodeRequestPram.getPluginType());
        boolean res = nodeOperate.stop(nodeRequestPram.getReq());
        if (res) {
            return Response.Success();
        }
        return Response.Warn("stop fail");
    }

    @RequestMapping(value = "/nodeRestart", method = RequestMethod.POST)
    @ResponseBody
    public Response nodeRestart(@RequestBody NodeRequestPram nodeRequestPram) {
        nodeOperate = nodeManager.factoryOperate(nodeRequestPram.getPluginType());
        boolean res = nodeOperate.restart(nodeRequestPram.getReq());
        if (res) {
            return Response.Success();
        }
        return Response.Warn("restart fail");
    }


    @RequestMapping(value = "/nodeRemove", method = RequestMethod.POST)
    @ResponseBody
    public Response nodeRemove(@RequestBody NodeRequestPram nodeRequestPram) {
        nodeOperate = nodeManager.factoryOperate(nodeRequestPram.getPluginType());
        boolean res = nodeOperate.remove(nodeRequestPram.getReq());
        if (res) {
            return Response.Success();
        }
        return Response.Warn("delete fail");
    }

    @RequestMapping(value = "/nodeInstall", method = RequestMethod.POST)
    @ResponseBody
    public Response nodeInstall(@RequestBody NodeRequestPram nodeRequestPram) {
        logger.info("Install: " + nodeRequestPram);
        nodeOperate = nodeManager.factoryOperate(nodeRequestPram.getPluginType());
        boolean res = nodeOperate.install(nodeRequestPram.getReq());
        if (res) {
            return Response.Success();
        }
        return Response.Warn("install fail");
    }

    @RequestMapping(value = "/nodePullImage", method = RequestMethod.POST)
    @ResponseBody
    public Response nodePullImage(@RequestBody NodeRequestPram nodeRequestPram) {
        nodeOperate = nodeManager.factoryOperate(nodeRequestPram.getPluginType());
        boolean res = nodeOperate.pullImage(nodeRequestPram.getReq());
        if (res) {
            return Response.Success();
        }
        return Response.Warn("pull image is fail");
    }

    @RequestMapping(value = "/humpbacknodeCheckAccess", method = RequestMethod.POST)
    @ResponseBody
    public Response humpbacknodeCheckAccess(@RequestBody JSONObject reqPram) {
        String checkRes = nodeManager.humpbackManager.checkAccess(reqPram);
        if (StringUtils.isBlank(checkRes)) {
            return Response.Success();
        }
        return Response.Error(checkRes);
    }

    @RequestMapping(value = "/dockernodeCheckAccess", method = RequestMethod.POST)
    @ResponseBody
    public Response dockernodeCheckAccess(@RequestBody JSONObject reqPram) {
        String checkRes = nodeManager.dockerManager.checkAccess(reqPram);
        if (StringUtils.isBlank(checkRes)) {
            return Response.Success();
        }
        return Response.Error(checkRes);
    }
}
