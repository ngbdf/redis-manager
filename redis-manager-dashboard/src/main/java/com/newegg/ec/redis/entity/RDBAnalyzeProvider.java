package com.newegg.ec.redis.entity;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

/**
 * @author kz37
 * @version 1.0.0
 */
public class RDBAnalyzeProvider extends SQL {
    private static final String TABLE_NAME = "rdb_analyze";

    public String updateRdbAnalyze(@Param("rdbAnalyze") RDBAnalyze rdbAnalyze) {
        return new SQL(){{
            UPDATE(TABLE_NAME);
            SET("auto_analyze = #{rdbAnalyze.autoAnalyze}");
            SET("schedule = #{rdbAnalyze.schedule}");
            SET("dataPath = #{rdbAnalyze.dataPath}");
            SET("prefixes = #{rdbAnalyze.prefixes}");
            SET("is_report = #{rdbAnalyze.report}");
            SET("mailTo = #{rdbAnalyze.mailTo}");
            SET("analyzer = #{rdbAnalyze.analyzer}");
            WHERE("id = #{rdbAnalyze.id}");
        }}.toString();
    }
}
