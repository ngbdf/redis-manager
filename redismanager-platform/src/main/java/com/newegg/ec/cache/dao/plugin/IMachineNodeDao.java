package com.newegg.ec.cache.dao.plugin;

import com.newegg.ec.cache.core.entity.model.plugin.MachineNode;
import com.newegg.ec.cache.core.entity.model.plugin.Node;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lf52 on 2018/5/9.
 */
@Repository
public interface IMachineNodeDao {

    /**
     * get MachineNode list
     *
     * @param cluster_id
     * @return
     */
    public List<Node> getMachineNodeList(int cluster_id);

    /**
     * get a machineNode by id
     *
     * @param id
     * @return
     */
    public MachineNode getMachineNode(int id);

    /**
     * remove machineNode
     *
     * @param id
     * @return
     */
    public Boolean removeMachineNode(int id);

    /**
     * add a machineNode
     *
     * @param machineNode
     * @return
     */
    public Boolean addMachineNode(MachineNode machineNode);

    /**
     * remove MachineNode By ClusterId
     * @param id
     * @return
     */
    public Boolean removeMachineNodeByClusterId(int id);

}
