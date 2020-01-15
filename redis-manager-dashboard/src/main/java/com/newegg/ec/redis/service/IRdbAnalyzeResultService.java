package com.newegg.ec.redis.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeResult;
import com.newegg.ec.redis.entity.ReportData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRdbAnalyzeResultService {
    public void delete(Long id);

    public void add(RDBAnalyzeResult rdbAnalyzeResult);

    public List<RDBAnalyzeResult> selectList();

    public List<RDBAnalyzeResult> selectAllResultById(Long cluster_id);

    public List<RDBAnalyzeResult> selectAllResultByIdExceptLatest(Long cluster_id);

    public RDBAnalyzeResult selectLatestResultByRID(Long cluster_id);

    public RDBAnalyzeResult selectResultByRIDandSID(Long cluster_id, Long scheduleId);

    public List<RDBAnalyzeResult> selectByMap(Map<String, Object> map);

    public boolean checkResult(int result);

    public RDBAnalyzeResult reportDataWriteToDb(RDBAnalyze rdbAnalyze, Map<String, Set<String>> data);

    public List<JSONObject> getAllKeyPrefixByResult(String result);

    public Object getLineStringFromResult(Long id, Long scheduleId, String key) throws Exception;

    public JSONArray getJsonArrayFromResult(Long id, Long scheduleId, String key) throws Exception;

    public Object getListStringFromResult(Long id, Long scheduleId, String key) throws Exception;

    public JSONArray getJSONArrayFromResultByKey(String result, String key);

    public Object getTopKeyFromResultByKey(Long id, Long scheduleId, Long key) throws Exception;

    public JSONArray getTopKeyFromResultByKey(String result, Long startNum);

    public JSONArray getPrefixLineByCountOrMem(Long cluster_id, String type, int top, String prefixKey);

    public List<String> getcolumnKeyList(String prefixKey, List<JSONObject> resultObjecList, String columnName, int top);

    public List<JSONObject> getJSONObjList(JSONArray jsonArray);

    public String getSortColumn(String type);

    public JSONArray getPrefixType(Long id, Long scheduleId) throws Exception;

    public JSONArray getPrefixArrayAddMem(JSONArray count, Map<String, JSONObject> memMap, String column);

    public Map<String, JSONObject> getJsonObject(JSONArray jsonArray);

    public Map<String, ReportData> getReportDataLatest(Long cluster_id);

    void createRdbAnalyzeResultTable();
}
