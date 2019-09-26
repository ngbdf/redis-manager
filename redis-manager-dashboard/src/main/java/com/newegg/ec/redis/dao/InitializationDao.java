package com.newegg.ec.redis.dao;

import org.apache.ibatis.annotations.Mapper;
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
@Mapper
public interface InitializationDao {

    @Select("create TABLE IF NOT EXISTS `group` (" +
            "group_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_name varchar(255) NOT NULL, " +
            "group_info varchar(255) DEFAULT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (group_id), " +
            "UNIQUE KEY (group_name) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createGroupTable();

    @Select("create TABLE IF NOT EXISTS `user` ( " +
            "user_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "user_name varchar(255) NOT NULL, " +
            "password varchar(255) DEFAULT NULL, " +
            "token varchar(255) DEFAULT NULL, " +
            "head_pic varchar(255) NOT NULL, " +
            "email varchar(255) NOT NULL, " +
            "mobile varchar(20) NOT NULL, " +
            "user_type TINYINT(4) NOT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (user_id), " +
            "UNIQUE KEY `base_info` (user_name, mobile, email) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createUserTable();
    
    @Select("create TABLE IF NOT EXISTS `group_user`( " +
            "group_user_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "user_id integer(4) NOT NULL, " +
            "user_role varchar(20) NOT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (group_user_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createGroupUserTable();
    
    @Select("create TABLE IF NOT EXISTS `cluster`( " +
            "cluster_id integer(4) NOT NULL AUTO_INCREMENT COMMENT '自增ID', " +
            "group_id integer(4) NOT NULL, " +
            "user_id integer(4) NOT NULL, " +
            "cluster_token varchar(255) DEFAULT NULL, " +
            "cluster_name varchar(255) NOT NULL, " +
            "nodes varchar(255) NOT NULL, " +
            "redis_mode varchar(25) NOT NULL, " +
            "os varchar(255) NOT NULL, " +
            "redis_version varchar(25) NOT NULL, " +
            "image varchar(255) DEFAULT NULL, " +
            "initialized tinyint(1) NOT NULL, " +
            "total_keys bigint(20) NOT NULL, " +
            "total_expires bigint(20) NOT NULL, " +
            "db_size integer(8) NOT NULL, " +
            "cluster_status varchar(50) NOT NULL, " +
            "cluster_slots_assigned integer(4) NOT NULL, " +
            "cluster_slots_ok integer(4) NOT NULL, " +
            "cluster_slots_pfail integer(4) NOT NULL, " +
            "cluster_slots_fail integer(4) NOT NULL, " +
            "cluster_known_nodes integer(4) NOT NULL, " +
            "cluster_size integer(4) NOT NULL, " +
            "redis_password varchar(50) DEFAULT NULL, " +
            "installation_environment varchar(25) NOT NULL, " +
            "installation_type tinyint(1) NOT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (cluster_id), " +
            "UNIQUE KEY (cluster_name) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createClusterTable();

    @Select("create TABLE IF NOT EXISTS `machine`( " +
            "machine_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "machine_group_name varchar(255) NOT NULL, " +
            "group_id integer(4) NOT NULL, " +
            "host varchar(50) NOT NULL, " +
            "user_name varchar(50) NOT NULL, " +
            "password varchar(255) DEFAULT NULL, " +
            "token varchar(255) DEFAULT NULL, " +
            "machine_info varchar(255) DEFAULT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (machine_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createMachineTable();
    
}
