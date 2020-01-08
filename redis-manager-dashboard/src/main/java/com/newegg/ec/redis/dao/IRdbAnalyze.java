package com.newegg.ec.redis.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.newegg.ec.redis.entity.RDBAnalyze;
import org.apache.ibatis.annotations.Select;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 16:26
 */
public interface IRdbAnalyze extends BaseMapper<RDBAnalyze> {

    Integer updateRdbAnalyze();

    @Select("create TABLE IF NOT EXISTS `rdb_analyze`( " +
            "id integer AUTO_INCREMENT, " +
            "auto_analyze integer NOT NULL, " +
            "schedule varchar(255), " +
            "dataPath varchar(255), " +
            "prefixes varchar(255), " +
            "is_report integer, " +
            "mailTo varchar(255), " +
            "analyzer varchar(255), " +
            "pid integer, " + // redis id
            "PRIMARY KEY (id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createRdbAnalyzeTable();

    @Select("select id from rdb_analyze where pid = #{pid}")
    Long getRDBAnalyzeIdByPid(Long pid);

    @Select("select pid from rdb_analyze where id=#{id}")
    Long selectPidById(Long id);
}
