package com.newegg.ec.redis.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeResult;
import com.newegg.ec.redis.entity.ReportData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRdbAnalyzeResultService {
    void delete(Long id);
    void add(RDBAnalyzeResult rdbAnalyzeResult);
    List<RDBAnalyzeResult> selectList(Long groupId);
    RDBAnalyzeResult reportDataWriteToDb(RDBAnalyze rdbAnalyze, Map<String, Set<String>> data);
    Object getListStringFromResult(Long analyzeResultId, String key) throws Exception;
    JSONObject getPrefixLineByCountOrMem(Long analyzeResultId, String type, int top, String prefixKey);
    JSONArray getPrefixType(Long analyzeResultId) throws Exception;
    Map<String, ReportData> getReportDataLatest(Long clusterId);
    void createRdbAnalyzeResultTable();
    List<RDBAnalyzeResult> getAllAnalyzeResult(List<RDBAnalyzeResult> results, List<Cluster> clusters);
    List<RDBAnalyzeResult> selectAllRecentlyResultById(Long resultId);
    Object getTopKeyFromResultByKey(Long analyzeResultId, Long key) throws Exception;

}
