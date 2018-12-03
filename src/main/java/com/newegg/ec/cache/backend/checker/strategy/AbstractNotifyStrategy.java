package com.newegg.ec.cache.backend.checker.strategy;

import com.newegg.ec.cache.app.dao.IClusterCheckLogDao;
import com.newegg.ec.cache.app.dao.IClusterCheckRuleDao;
import com.newegg.ec.cache.app.dao.IClusterDao;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.ClusterCheckRule;
import com.newegg.ec.cache.app.util.DateUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lf52 on 2018/12/1.
 */
public abstract class AbstractNotifyStrategy implements NotifyStrategy,ApplicationListener<ContextRefreshedEvent> {

    private static ExecutorService pool;

    @Resource
    private IClusterDao clusterDao;

    @Resource
    private IClusterCheckRuleDao checkRuleDao;

    @Resource
    private IClusterCheckLogDao checkLogDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        pool = Executors.newFixedThreadPool(50);
    }

    @Override
    public final void notifyUser() {
        if (checkNotify()) {
            List<Cluster> clusterList = clusterDao.getClusterList(null);
            long time = DateUtil.getBeforeMinutesTime(10);
            for (Cluster cluster : clusterList) {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        //按规则检查日志
                        List<ClusterCheckRule> ruleList = checkRuleDao.getClusterRuleList(cluster.getId() + "");
                        for (ClusterCheckRule rule : ruleList) {
                            String formula = rule.getFormula();
                            Map<String, Object> param = new HashMap();
                            param.put("clusterId", cluster.getId());
                            param.put("updateTime", time);
                            param.put("formula", formula);
                            int warnsize = checkLogDao.getClusterCheckLogs(param).size();
                            if (warnsize > 0) {
                                alarmToUser(cluster, formula);
                            }
                        }
                    }
                });
            }
        }

    }

    /**
     * notify前置校验
     * @return
     */
    protected abstract boolean checkNotify();

    /**
     * 真正通知用户操作
     * @param cluster
     * @param formula
     */
    protected abstract void alarmToUser(Cluster cluster,String formula);

}
