package com.newegg.ec.redis.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeResult;
import com.newegg.ec.redis.entity.ReportData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 17:10
 */
public interface IRdbAnalyzeResultService {
    int totalCount = 7;

    void delete(Long id);

    void add(RDBAnalyzeResult rdbAnalyzeResult);

    List<RDBAnalyzeResult> selectAllResultByIdExceptLatest(Long redisInfoId);

    RDBAnalyzeResult selectLatestResultByRID(Long redisInfoId);

    RDBAnalyzeResult selectResultByRIDandSID(Long redisInfoId, Long scheduleId);

    List<RDBAnalyzeResult> selectByMap(Map<String, Object> map);

    RDBAnalyzeResult reportDataWriteToDb(RDBAnalyze rdbAnalyze, Map<String, Set<String>> data);

    List<JSONObject> getAllKeyPrefixByResult(String result);

    Object getLineStringFromResult(Long id, Long scheduleId, String key);

    JSONArray getJsonArrayFromResult(Long id, Long scheduleId, String key);

    Object getListStringFromResult(Long id, Long scheduleId, String key) throws Exception;

    JSONArray getJSONArrayFromResultByKey(String result, String key);

    Object getTopKeyFromResultByKey(Long id, Long scheduleId, Long key) throws Exception;

    JSONArray getTopKeyFromResultByKey(String result, Long startNum);

    JSONArray getPrefixLineByCountOrMem(Long pid, String type, int top, String prefixKey);

    Map<String, JSONObject> getMapJSONByResult(RDBAnalyzeResult rdbAnalyzeResult, JSONArray arrayResult);

    List<String> getcolumnKeyList(String prefixKey, List<JSONObject> resultObjecList, String columnName, int top);

    List<JSONObject> getJSONObjList(JSONArray jsonArray);

    String getSortColumn(String type);

    JSONArray getPrefixType(Long id, Long scheduleId) throws Exception;

    JSONArray getPrefixArrayAddMem(JSONArray count, Map<String, JSONObject> memMap, String column);

    Map<String, JSONObject> getJsonObject(JSONArray jsonArray);

    Map<String, ReportData> getReportDataLatest(Long pid);

    void createRdbAnalyzeResultTable();

}
