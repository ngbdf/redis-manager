package com.newegg.ec.redis.dao;

import org.apache.ibatis.annotations.Select;

/**
 * Create tables
 *
 * Tables:
 *      group
 *      user
 *      machine
 *      cluster
 *      node_info_#{clusterName}
 *      machine_node
 *      docker_node
 *      kubernetes_node
 *      slow_log
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface InitializationDao {

    @Select("")
    void createGroupTable();

}
