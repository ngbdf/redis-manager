package com.newegg.ec.redis.dao;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 16:26
 */
@Mapper
public interface IRdbAnalyze extends BaseMapper<RDBAnalyze> {

    @UpdateProvider(type = RDBAnalyzeProvider.class, method = "updateRdbAnalyze")
    Integer updateRdbAnalyze(@Param("rdbAnalyze") RDBAnalyze rdbAnalyze);

    @Select("select * from rdb_analyze")
    List<RDBAnalyze> queryList();

    @Select("create TABLE IF NOT EXISTS `rdb_analyze`( " +
            "id integer AUTO_INCREMENT, " +
            "auto_analyze integer NOT NULL, " +
            "schedule varchar(255), " +
            "dataPath varchar(255), " +
            "prefixes varchar(255), " +
            "is_report integer, " +
            "mailTo varchar(255), " +
            "analyzer varchar(255), " +
            "cluster_id integer, " + // redis id
            "PRIMARY KEY (id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createRdbAnalyzeTable();

    @Select("select id from rdb_analyze where cluster_id = #{cluster_id}")
    Long getRDBAnalyzeIdByCluster_id(Long cluster_id);

    @Select("select cluster_id from rdb_analyze where id=#{id}")
    Long selectClusterIdById(Long id);

    @Select("select * from rdb_analyze where cluster_id = #{cluster_id}")
    RDBAnalyze getRDBAnalyzeByCluster_id(Long cluster_id);

    @Select("select * from rdb_analyze where id = #{id}")
    RDBAnalyze getRDBAnalyzeById(Long id);

    @Insert("insert into rdb_analyze (auto_analyze,schedule,dataPath,prefixes,is_report,mailTo,analyzer,cluster_id,group_id) " +
            "values (#{autoAnalyze},#{schedule},#{dataPath},#{prefixes},#{report},#{mailTo},#{analyzer},#{clusterId},#{groupId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(RDBAnalyze rdbAnalyze);

}
