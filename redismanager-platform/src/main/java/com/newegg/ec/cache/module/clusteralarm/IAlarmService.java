package com.newegg.ec.cache.module.clusteralarm;

import com.newegg.ec.cache.core.entity.model.ClusterCheckLog;
import com.newegg.ec.cache.core.entity.model.ClusterCheckRule;

import java.util.List;

/**
 * Created by lf52 on 2019/2/26.
 */
public interface IAlarmService {

    public List<ClusterCheckRule> getRuleList(String clusterId);

    public Boolean addRule(ClusterCheckRule rule);

    public Boolean checkRule(ClusterCheckRule rule);

    public Boolean deleteRule(String ruleId);

    public List<ClusterCheckLog> getCaseList(String clusterId);

    public void deleteCaseLog(String logId);

    public void deleteAllLog(String clusterId);

    public Integer countTotalLog(List<String> clusterIds);

    public Integer countWarningLogByClusterId(Integer clusterId);

}
