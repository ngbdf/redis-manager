package com.newegg.ec.app.dao;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.core.dao.IClusterCheckRuleDao;
import com.newegg.ec.cache.core.entity.model.ClusterCheckRule;
import com.newegg.ec.cache.core.utils.CommonUtil;
import com.newegg.ec.cache.core.utils.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterCheckRuleDaoTest {

    @Resource
    private IClusterCheckRuleDao ruleDao;

    @Test
    public void addTest(){
        ClusterCheckRule rule= new ClusterCheckRule();
        rule.setId(CommonUtil.getUuid());
        rule.setClusterId("test-tc72");
        rule.setFormula("@{totalKeys}>10000");
        rule.setLimitName("totalKeys");
        rule.setDescription("totalKeys is to more");
        rule.setUpdateTime(DateUtil.getTime());
        System.out.println(ruleDao.addClusterCheckRule(rule));
    }

    @Test
    public void getRuleTest(){
        System.out.println(ruleDao.getClusterRule("7b919524-1b0a-4e8b-9a30-c6e1ab4c6606"));
    }

    @Test
    public void getRuleListTest(){
        System.out.println(ruleDao.getClusterRuleList("ssecbigdata").size());
    }

    @Test
    public void updateRuleTest(){
        ClusterCheckRule rule =  ruleDao.getClusterRule("7b919524-1b0a-4e8b-9a30-c6e1ab4c6606");
        rule.setFormula("@{mem_fragmentation_ratio}>4.0");
        System.out.println(ruleDao.updateClusterCheckRule(rule));
    }

    @Test
    public void delRuleTest(){
        Map<String,Object> param = new HashMap();
        param.put("clusterId","ssecbigdata");
        System.out.println(ruleDao.delClusterCheckRule(param));
    }

}
