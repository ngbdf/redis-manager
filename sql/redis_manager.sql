CREATE DATABASE `redis_manager` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

-- 最后创建索引和唯一键

-- group
CREATE TABLE IF NOT EXISTS `group`  (
    group_id integer（4） NOT NULL AUTO_INCREMENT = 1000 COMMENT '自增ID',
    group_name varchar(255) NOT NULL UNIQUE COMMENT '组名',
    group_info varchar(255) DEFAULT NULL COMMENT '组描述',
    update_time datetime(0) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (group_id)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- user
CREATE TABLE IF NOT EXISTS `user`  (
    user_id integer(4) NOT NULL AUTO_INCREMENT = 1000 COMMENT '自增ID',
    user_name varchar(255) NOT NULL UNIQUE COMMENT '用户名',
    password varchar(255) DEFAULT NULL COMMENT '密码',
    token varchar(255) DEFAULT NULL COMMENT '其他登陆方式',
    user_role varchar(50) NOT NULL COMMENT '用户角色',
    head_pic varchar(255) NOT NULL COMMENT '头像',
    email varchar(255) NOT NULL COMMENT '邮箱',
    phone_number varchar(20) NOT NULL COMMENT '手机号',
    user_type TINYINT(4) NOT NULL COMMENT '用户类型',
    update_time datetime(0) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (user_id)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- group_user
CREATE TABLE IF NOT EXISTS `group_user`  (
    group_user_id integer NOT NULL AUTO_INCREMENT = 1000 COMMENT '自增ID',
    group_id integer(4) NOT NULL COMMENT '组ID',
    user_id integer(4) NOT NULL COMMENT '用户ID',
    update_time datetime(0) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (group_user_id)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;