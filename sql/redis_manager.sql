create DATABASE `redis_manager` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

-- 最后创建索引和唯一键

-- group
create TABLE IF NOT EXISTS `group` (
    group_id integer(4) NOT NULL AUTO_INCREMENT,
    group_name varchar(255) NOT NULL,
    cluster_number integer(2) NOT NULL,
    good_cluster_number integer(2) NOT NULL,
    user_number integer(2) NOT NULL,
    group_info varchar(255),
    update_time datetime(0) NOT NULL,
    PRIMARY KEY (group_id),
    UNIQUE KEY (group_name)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- user
create TABLE IF NOT EXISTS `user`(
    user_id integer(4) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    user_name varchar(255) NOT NULL,
    password varchar(255) DEFAULT NULL COMMENT '密码',
    token varchar(255) DEFAULT NULL COMMENT '其他登陆方式',
    user_role varchar(50) NOT NULL COMMENT '用户角色',
    head_pic varchar(255) NOT NULL COMMENT '头像',
    email varchar(255) NOT NULL COMMENT '邮箱',
    phone_number varchar(20) NOT NULL COMMENT '手机号',
    user_type TINYINT(4) NOT NULL COMMENT '用户类型',
    update_time datetime(0) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY `base_info` (user_name, phone_number, email)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- group_user
create TABLE IF NOT EXISTS `group_user`(
    group_user_id integer(4) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    group_id integer(4) NOT NULL COMMENT '组ID',
    user_id integer(4) NOT NULL COMMENT '用户ID',
    update_time datetime(0) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (group_user_id)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- cluster
create TABLE IF NOT EXISTS `cluster`(
    cluster_id integer(4) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    group_id integer(4) NOT NULL,
    admins varchar(255) NOT NULL,
    cluster_token varchar(255) DEFAULT NULL,
    nodes varchar(255) NOT NULL,
    redis_mode varchar(25) NOT NULL,
    os varchar(255) NOT NULL,
    redis_version varchar(25) NOT NULL,
    total_keys integer(4) NOT NULL,
    total_expires integer(4) NOT NULL,
    cluster_state tinyint(1) NOT NULL,
    cluster_slots_assigned integer(4) NOT NULL,
    cluster_slots_ok integer(4) NOT NULL,
    cluster_slots_pfail integer(4) NOT NULL,
    cluster_slots_fail integer(4) NOT NULL,
    cluster_known_nodes integer(4) NOT NULL,
    cluster_size integer(2) NOT NULL,
    db_number integer(2) NOT NULL,
    redis_password varchar(25) DEFAULT NULL,
    installation_env varchar(25) NOT NULL,
    installation_type tinyint(1) NOT NULL,
    update_time datetime(0) NOT NULL,
    PRIMARY KEY (cluster_id),
    UNIQUE KEY (cluster_name)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- node_info_${clusterId}
create TABLE IF NOT EXISTS `node_info_0`(
    `info_id` integer(4) NOT NULL AUTO_INCREMENT,
    `node` varchar(50) NOT NULL,
    `data_type` varchar(50) NOT NULL,
    `time_type` varchar(50) NOT NULL,
    `response_time` integer(4) NOT NULL,
    `connected_clients` integer(4) NOT NULL,
    `client_longest_output_list` integer(4) NOT NULL,
    `client_biggest_input_buf` integer(4) NOT NULL,
    `blocked_clients` integer(4) NOT NULL,
    `used_memory` integer(4) NOT NULL,
    `used_memory_rss` integer(4) NOT NULL,
    `used_memory_overhead` integer(4) NOT NULL,
    `used_memory_dataset` integer(4) NOT NULL,
    `used_memory_dataset_perc` integer(4) NOT NULL,
    `mem_fragmentation_ratio` double(6, 2) NOT NULL,
    `total_connections_received` integer(4) NOT NULL,
    `connections_received` integer(4) NOT NULL,
    `total_commands_processed` integer(4) NOT NULL,
    `commands_processed` integer(4) NOT NULL,
    `total_net_input_bytes` integer(4) NOT NULL,
    `net_input_bytes` integer(4) NOT NULL,
    `total_net_output_bytes` integer(4) NOT NULL,
    `net_output_bytes` integer(4) NOT NULL,
    `keyspace_hits` integer(4) NOT NULL,
    `keyspace_misses` integer(4) NOT NULL,
    `keyspace_hits_ratio` double(6, 2) NOT NULL,
    `used_cpu_sys` double(6, 2) NOT NULL,
    `keys` integer(4) NOT NULL,
    `expires` integer(4) NOT NULL,
    `update_time` datetime(0) NOT NULL,
    PRIMARY KEY (`node_info_id`),
    INDEX `multiple_query` (`update_time`, `data_type`, `time_type`, `node`)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;



