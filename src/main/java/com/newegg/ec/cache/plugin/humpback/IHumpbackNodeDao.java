package com.newegg.ec.cache.plugin.humpback;

import com.newegg.ec.cache.plugin.basemodel.Node;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lzz on 2018/4/20.
 */
@Repository
public interface IHumpbackNodeDao {

    /**
     * get humbackNode list
     *
     * @param cluster_id
     * @return
     */
    public List<Node> getHumbackNodeList(int cluster_id);

    /**
     * get a humpbackNode by id
     *
     * @param id
     * @return
     */
    public HumpbackNode getHumpbackNode(int id);

    /**
     * remove humpbackNode
     *
     * @param id
     * @return
     */
    public Boolean removeHumbackNode(int id);

    /**
     * add a humpbackNode
     *
     * @param humpbackNode
     * @return
     */
    public Boolean addHumbackNode(HumpbackNode humpbackNode);

    /**
     * remove HumbackNode By ClusterId
     * @param id
     * @return
     */
    public Boolean removeHumbackNodeByClusterId(int id);
}
