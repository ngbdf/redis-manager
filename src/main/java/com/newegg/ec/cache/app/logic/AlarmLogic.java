package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.dao.IClusterCheckLogDao;
import com.newegg.ec.cache.app.dao.IClusterCheckRuleDao;
import com.newegg.ec.cache.app.model.ClusterCheckLog;
import com.newegg.ec.cache.app.model.ClusterCheckRule;
import com.newegg.ec.cache.app.util.CommonUtil;
import com.newegg.ec.cache.app.util.DateUtil;
import com.newegg.ec.cache.app.util.MathExpressionCalculateUtil;
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
public class AlarmLogic {
    @Autowired
    IClusterCheckRuleDao clusterCheckRuleDao;

    @Autowired
    IClusterCheckLogDao clusterCheckLogDao;


    public List<ClusterCheckRule> getRuleList(String clusterId) {
        return clusterCheckRuleDao.getClusterRuleList(clusterId);
    }

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

    public Boolean checkRule(ClusterCheckRule rule) {
        return MathExpressionCalculateUtil.checkRule(rule.getFormula());
    }

    public Boolean deleteRule(String ruleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", ruleId);
        return clusterCheckRuleDao.delClusterCheckRule(params);
    }

    public List<ClusterCheckLog> getCaseList(String clusterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("clusterId", clusterId);
        return clusterCheckLogDao.getClusterCheckLogs(params);
    }

    public void deleteCaseLog(String logId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", logId);
        clusterCheckLogDao.delLogs(params);
    }

    public void deleteAllLog(String clusterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("clusterId", clusterId);
        clusterCheckLogDao.delLogs(params);
    }

    public Integer countTotalLog(List<String> clusterIds) {
        Integer count = 0;
        if (clusterIds != null && clusterIds.size() > 0) {
            count = clusterCheckLogDao.countTotalWarningLog(clusterIds);
        }
        return count;
    }

    public Integer countWarningLogByClusterId(Integer clusterId) {
        int count = 0;
        if (StringUtils.isNotBlank(clusterId.toString())) {
            count = clusterCheckLogDao.countWarningLogByClusterId(clusterId);
        }
        return count;
    }
}
