package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.app.model.ClusterCheckLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2018/4/26.
 */
@Repository
public interface IClusterCheckLogDao {
    /**
     * 添加日志
     * @param log
     * @return
     */
    Boolean addClusterCheckLog(ClusterCheckLog log);

    /**
     * 条件检索日志
     * @param params
     * @return
     */
    List<ClusterCheckLog> getClusterCheckLogs(@Param("params")Map<String,Object> params);

    /**
     * 按条件删除日志
     * @param params
     * @return
     */
    void delLogs(@Param("params")Map<String,Object> params);

    /**
     * 将日志的isChecked置为true
     * @param params
     * @return
     */
    Boolean checkWarningLogs(@Param("params")Map<String,Object> params);


    Integer countTotalWarningLog(List<String> clusterIds);

    Integer countWarningLogByClusterId(Integer clusterId);
}
