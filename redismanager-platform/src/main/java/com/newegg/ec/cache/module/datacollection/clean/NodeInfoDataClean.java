package com.newegg.ec.cache.module.datacollection.clean;

import com.newegg.ec.cache.dao.IClusterDao;
import com.newegg.ec.cache.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/5/29.
 */
@Component
public class NodeInfoDataClean {

    private static final Log logger = LogFactory.getLog(NodeInfoDataClean.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Resource
    private IClusterDao clusterDao;

    @Value("${cache.mysql.database.name}")
    private String databaseName;

    @Value("${cache.mysql.clearn.day}")
    private int clearnDay;

    @Scheduled(fixedRateString = "${schedule.redisinfo.clean}")
    public void clean() {

        try {
            String sql = "show tables";
            List<Map<String, Object>> tableList = jdbcTemplate.queryForList(sql);
            int sevcenTime = DateUtil.getTime() - 24 * 60 * 60 * clearnDay;
            for (Map<String, Object> table : tableList) {
                String tableField = "Tables_in_" + databaseName;
                String tableName = (String) table.get(tableField);
                if (tableName.contains("node_info")) {
                    String deleteSql = "delete from " + tableName + " where add_time < " + sevcenTime;
                    jdbcTemplate.execute(deleteSql);
                }
            }
        } catch (Exception e) {
            logger.error("Clean History Redis Info Data Error",e);
        }

    }

}
