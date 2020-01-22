CREATE DATABASE `redis_manager_refactor` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

create TABLE IF NOT EXISTS `sentinel_masters` (
sentinel_master_id integer(4) NOT NULL AUTO_INCREMENT,
cluster_id integer(4) NOT NULL,
group_id integer(4) NOT NULL,
user_id integer(4) NOT NULL,
master_name varchar(255) NOT NULL,
master_node varchar(255) NOT NULL,
last_master_node varchar(255) NOT NULL,
flags varchar(255) NOT NULL,
s_down_time bigint(20) NOT NULL,
o_down_time bigint(20) NOT NULL,
down_after_milliseconds bigint(20) NOT NULL,
num_slaves integer(4) NOT NULL,
quorum integer(4) NOT NULL,
failover_timeout bigint(4) NOT NULL,
parallel-syncs integer(4) NOT NULL,
update_time datetime(0) NOT NULL,
PRIMARY KEY (sentinel_master_id)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;