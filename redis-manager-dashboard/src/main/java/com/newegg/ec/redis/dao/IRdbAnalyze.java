package com.newegg.ec.redis.dao;


import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 16:26
 */
@Mapper
public interface IRdbAnalyze {
    @Update("UPDATE rdb_analyze SET schedule = #{schedule},auto_analyze=#{autoAnalyze}, " +
            "dataPath=#{dataPath}," +
            "prefixes=#{prefixes},is_report=#{report},analyzer=#{analyzer},mailTo=#{mailTo}" +
            "WHERE cluster_id = #{clusterId} AND group_id = #{groupId}")
    Integer updateRdbAnalyze(RDBAnalyze rdbAnalyze);

    @Select("select * from rdb_analyze where group_id=#{groupId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "schedule", property = "schedule"),
            @Result(column = "auto_analyze", property = "autoAnalyze"),
            @Result(column = "dataPath", property = "dataPath"),
            @Result(column = "cluster_id", property = "cluster",one=@One(select="com.newegg.ec.redis.dao.IClusterDao.selectClusterById")),
            @Result(column = "prefixes", property = "prefixes"),
            @Result(column = "is_report", property = "report"),
            @Result(column = "analyzer", property = "analyzer"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "mailTo", property = "mailTo"),
            @Result(column = "group_id", property = "groupId")
    })
    List<RDBAnalyze> queryList(@Param("groupId") Long groupId);


    @Select("select * from rdb_analyze")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "schedule", property = "schedule"),
            @Result(column = "auto_analyze", property = "autoAnalyze"),
            @Result(column = "dataPath", property = "dataPath"),
            @Result(column = "cluster_id", property = "cluster",one=@One(select="com.newegg.ec.redis.dao.IClusterDao.selectClusterById")),
            @Result(column = "prefixes", property = "prefixes"),
            @Result(column = "is_report", property = "report"),
            @Result(column = "analyzer", property = "analyzer"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "mailTo", property = "mailTo"),
            @Result(column = "group_id", property = "groupId")
    })
    List<RDBAnalyze> list();

    @Select("select * from rdb_analyze where id=#{id}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "schedule", property = "schedule"),
            @Result(column = "auto_analyze", property = "autoAnalyze"),
            @Result(column = "dataPath", property = "dataPath"),
            @Result(column = "cluster_id", property = "cluster",one=@One(select="com.newegg.ec.redis.dao.IClusterDao.selectClusterById")),
            @Result(column = "prefixes", property = "prefixes"),
            @Result(column = "is_report", property = "report"),
            @Result(column = "analyzer", property = "analyzer"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "mailTo", property = "mailTo"),
            @Result(column = "group_id", property = "groupId")
    })
    RDBAnalyze selectById(Long id);

    @Select("CREATE TABLE IF NOT EXISTS `rdb_analyze` (" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT," +
            "  `auto_analyze` int(11) NOT NULL," +
            "  `schedule` varchar(255) DEFAULT NULL," +
            "  `dataPath` varchar(255) DEFAULT NULL," +
            "  `prefixes` varchar(255) DEFAULT NULL," +
            "  `is_report` int(11) DEFAULT NULL," +
            "  `mailTo` varchar(255) DEFAULT NULL," +
            "  `analyzer` varchar(255) DEFAULT NULL," +
            "  `cluster_id` int(11) DEFAULT NULL," +
            "  `group_id` int(11) DEFAULT NULL," +
            "  PRIMARY KEY (`id`)" +
            ") ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;")
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

    @Delete("DELETE FROM rdb_analyze WHERE id = #{id}")
    Integer delete(Long id);

    @Select("select count(1) from rdb_analyze where group_id=#{groupId} and cluster_id=#{clusterId}")
    int exits(RDBAnalyze rdbAnalyze);
}
