package com.newegg.ec.cache.module.clusterbuild.plugins;

import com.newegg.ec.cache.core.entity.model.plugin.Node;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * Created by lzz on 2018/4/20.
 */
public interface INodeOperate {
    String checkAccess(JSONObject reqParam);

    boolean pullImage(JSONObject pullParam);

    boolean install(JSONObject installParam);

    boolean start(JSONObject startParam);

    boolean stop(JSONObject stopParam);

    boolean restart(JSONObject restartParam);

    boolean remove(JSONObject removePram);

    List<String> getImageList();

    List<Node> getNodeList(int clusterId);
}
