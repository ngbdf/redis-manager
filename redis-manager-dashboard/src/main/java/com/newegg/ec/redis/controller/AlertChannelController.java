package com.newegg.ec.redis.controller;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/10/4
 */
@RequestMapping("/alert/channel/*")
@Controller
public class AlertChannelController {

    @Autowired
    private IAlertChannelService alertChannelService;

    @Autowired
    private IClusterService clusterService;

    @RequestMapping(value = "/getAlertChannel/group/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertChannelByGroupId(@PathVariable("groupId") Integer groupId) {
        List<AlertChannel> alertChannelList = alertChannelService.getAlertChannelByGroupId(groupId);
        return alertChannelList != null ? Result.successResult(alertChannelList) : Result.failResult();
    }

    @RequestMapping(value = "/getAlertChannel/cluster/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertChannelByClusterId(@PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterById(clusterId);
        String channelIds = cluster.getChannelIds();
        if (Strings.isNullOrEmpty(channelIds)) {
            return Result.successResult();
        }
        String[] channelIdArr = SignUtil.splitByCommas(channelIds);
        List<Integer> channelIdList = new ArrayList<>();
        for (String channelId : channelIdArr) {
            channelIdList.add(Integer.parseInt(channelId));
        }
        List<AlertChannel> alertChannelList = alertChannelService.getAlertChannelByIds(channelIdList);
        boolean result = alertChannelList != null;
        // 每次检查是否有无用的 channel
        if (result) {
            List<Integer> realChannelIdList = new ArrayList<>();
            alertChannelList.forEach(alertChannel -> realChannelIdList.add(alertChannel.getChannelId()));
            boolean needUpdate = false;
            for (Integer channelId : channelIdList) {
                if (!realChannelIdList.contains(channelId)) {
                    needUpdate = true;
                    break;
                }
            }
            if (needUpdate) {
                StringBuilder newChannelIds = new StringBuilder();
                realChannelIdList.forEach(channelId -> newChannelIds.append(channelId).append(SignUtil.COMMAS));
                cluster.setChannelIds(newChannelIds.toString());
                clusterService.updateClusterChannelIds(cluster);
            }
        }
        return result ? Result.successResult(alertChannelList) : Result.failResult();
    }

    @RequestMapping(value = "/getAlertChannelNotUsed/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertChannelNotUsed(@PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterById(clusterId);
        String channelIds = cluster.getChannelIds();
        Integer groupId = cluster.getGroupId();
        if (Strings.isNullOrEmpty(channelIds)) {
            List<AlertChannel> alertChannelList = alertChannelService.getAlertChannelByGroupId(groupId);
            return alertChannelList != null ? Result.successResult(alertChannelList) : Result.failResult();
        }
        String[] channelIdArr = SignUtil.splitByCommas(channelIds);
        List<Integer> channelIdList = new ArrayList<>();
        for (String channelId : channelIdArr) {
            channelIdList.add(Integer.parseInt(channelId));
        }
        List<AlertChannel> alertChannelList = alertChannelService.getAlertChannelNotUsed(groupId, channelIdList);
        return alertChannelList != null ? Result.successResult(alertChannelList) : Result.failResult();
    }

    @RequestMapping(value = "/getAlertChannel/{channelId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertChannel(@PathVariable("channelId") Integer channelId) {
        AlertChannel alertChannel = alertChannelService.getAlertChannelById(channelId);
        return alertChannel != null ? Result.successResult(alertChannel) : Result.failResult();
    }

    @RequestMapping(value = "/addAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result addAlertChannel(@RequestBody AlertChannel alertChannel) {
        boolean result = alertChannelService.addAlertChannel(alertChannel);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/updateAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result updateAlertChannel(@RequestBody AlertChannel alertChannel) {
        boolean result = alertChannelService.updateAlertChannel(alertChannel);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertChannel(@RequestBody AlertChannel alertChannel) {
        boolean result = alertChannelService.deleteAlertChannelById(alertChannel.getChannelId());
        return result ? Result.successResult() : Result.failResult();
    }

}
