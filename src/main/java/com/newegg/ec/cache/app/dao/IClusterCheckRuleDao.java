package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.app.model.ClusterCheckRule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2018/4/26.
 */
@Repository
public interface IClusterCheckRuleDao {
    /**
     * 条件获取所有规则
     *
     * @param clusterId
     * @return
     */
    List<ClusterCheckRule> getClusterRuleList(String clusterId);

    /**
     * 根据id获取某条规则
     *
     * @param id
     * @return
     */
    ClusterCheckRule getClusterRule(String id);

    /**
     * 添加一条规则
     *
     * @param rule
     * @return
     */
    Boolean addClusterCheckRule(ClusterCheckRule rule);

    /**
     * 修改规则
     *
     * @param rule
     * @return
     */
    Boolean updateClusterCheckRule(ClusterCheckRule rule);

    /**
     * 删除规则（1.指定id删除 2.删除某个cluster的所有规则3.删除所有）
     *
     * @param params
     * @return
     */
    Boolean delClusterCheckRule(@Param("params") Map<String, Object> params);
}
