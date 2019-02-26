package com.newegg.ec.cache.module.clusteralarm;

import com.newegg.ec.cache.core.entity.model.ClusterCheckLog;
import com.newegg.ec.cache.core.entity.model.ClusterCheckRule;
import com.newegg.ec.cache.dao.IClusterCheckLogDao;
import com.newegg.ec.cache.dao.IClusterCheckRuleDao;
import com.newegg.ec.cache.util.CommonUtil;
import com.newegg.ec.cache.util.DateUtil;
import com.newegg.ec.cache.util.MathExpressionCalculateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tc72 on 2018/5/5.
 */
@Component
public class AlarmService implements IAlarmService {
    @Autowired
    IClusterCheckRuleDao clusterCheckRuleDao;

    @Autowired
    IClusterCheckLogDao clusterCheckLogDao;

    @Override
    public List<ClusterCheckRule> getRuleList(String clusterId) {
        return clusterCheckRuleDao.getClusterRuleList(clusterId);
    }

    @Override
    public Boolean addRule(ClusterCheckRule rule) {
        Boolean check = MathExpressionCalculateUtil.checkRule(rule.getFormula());
        if (check) {
            rule.setId(CommonUtil.getUuid());
            rule.setUpdateTime(DateUtil.getTime());
            return clusterCheckRuleDao.addClusterCheckRule(rule);
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean checkRule(ClusterCheckRule rule) {
        return MathExpressionCalculateUtil.checkRule(rule.getFormula());
    }

    @Override
    public Boolean deleteRule(String ruleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", ruleId);
        return clusterCheckRuleDao.delClusterCheckRule(params);
    }

    @Override
    public List<ClusterCheckLog> getCaseList(String clusterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("clusterId", clusterId);
        return clusterCheckLogDao.getClusterCheckLogs(params);
    }

    @Override
    public void deleteCaseLog(String logId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", logId);
        clusterCheckLogDao.delLogs(params);
    }

    @Override
    public void deleteAllLog(String clusterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("clusterId", clusterId);
        clusterCheckLogDao.delLogs(params);
    }

    @Override
    public Integer countTotalLog(List<String> clusterIds) {
        Integer count = 0;
        if (clusterIds != null && clusterIds.size() > 0) {
            count = clusterCheckLogDao.countTotalWarningLog(clusterIds);
        }
        return count;
    }

    @Override
    public Integer countWarningLogByClusterId(Integer clusterId) {
        int count = 0;
        if (StringUtils.isNotBlank(clusterId.toString())) {
            count = clusterCheckLogDao.countWarningLogByClusterId(clusterId);
        }
        return count;
    }

}
