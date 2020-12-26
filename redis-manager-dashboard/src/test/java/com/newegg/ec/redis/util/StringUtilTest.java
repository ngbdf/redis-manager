package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.RedisManagerApplication;
import com.newegg.ec.redis.entity.RDBAnalyzeResult;
import com.newegg.ec.redis.service.impl.RdbAnalyzeResultService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RedisManagerApplication.class)
public class StringUtilTest {

    @Autowired
    private RdbAnalyzeResultService rdbAnalyzeResultService;

    @Test
    public void sortStr() {
        RDBAnalyzeResult rdbAnalyzeResults = rdbAnalyzeResultService.selectResultById(850L);
        String analyze = rdbAnalyzeResults.getResult();
        Map<String, String> map = new HashMap<>();
        JSONObject object = JSONObject.parseObject(analyze);
        object.keySet().forEach(key -> {
            map.put(key, object.getString(key));
        });
        rdbAnalyzeResultService.combinePrefixKey(map).forEach((k,v)->{
            System.out.println(k+":"+v);
        });
    }
}
