package com.newegg.ec.cache.dao;

import com.newegg.ec.cache.core.entity.annotation.mysql.MysqlUtil;
import com.newegg.ec.cache.core.entity.model.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class NodeInfoDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean createTable(String tableName) {
        boolean res = true;
        try {
            Class claz = NodeInfo.class;
            String createTable = MysqlUtil.createTableSql(claz, tableName);
            jdbcTemplate.execute(createTable);
        } catch (Exception e) {
            res = false;
        }
        return res;
    }

    public boolean dropTable(String tableName) {
        boolean res = false;
        try {
            String sql = "drop table " + tableName;
            jdbcTemplate.execute(sql);
            res = true;
        } catch (Exception e) {

        }
        return res;
    }
}
