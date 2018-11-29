package com.newegg.ec.cache.plugin.docker;

import com.newegg.ec.cache.plugin.basemodel.Node;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lf52 on 2018/5/9.
 */
@Repository
public interface IDockerNodeDao {

    /**
     * get DockerNode list
     *
     * @param cluster_id
     * @return
     */
    public List<Node> getDockerNodeList(int cluster_id);

    /**
     * get a DockerNode by id
     *
     * @param id
     * @return
     */
    public DockerNode getDockerNode(int id);

    /**
     * remove DockerNode
     *
     * @param id
     * @return
     */
    public Boolean removeDockerNode(int id);

    /**
     * add a DockerNode
     *
     * @param DockerNode
     * @return
     */
    public Boolean addDockerNode(DockerNode DockerNode);

    /**
     * remove DockerNode By ClusterId
     * @param id
     * @return
     */
    public Boolean removeDockerNodeByClusterId(int id);
}
