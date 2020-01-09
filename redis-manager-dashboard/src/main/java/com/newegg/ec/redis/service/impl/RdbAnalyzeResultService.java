package com.newegg.ec.redis.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.dao.IRdbAnalyzeResult;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeResult;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.service.IRdbAnalyzeResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 17:57
 */
@Service
public class RdbAnalyzeResultService implements IRdbAnalyzeResultService {

    @Autowired
    IRdbAnalyzeResult iRdbAnalyzeResult;

    @Override
    public void delete(Long id) {

    }

    @Override
    public void add(RDBAnalyzeResult rdbAnalyzeResult) {

    }

    @Override
    public List<RDBAnalyzeResult> selectAllResultByIdExceptLatest(Long redisInfoId) {
        return null;
    }

    @Override
    public RDBAnalyzeResult selectLatestResultByRID(Long redisInfoId) {
        return null;
    }

    @Override
    public RDBAnalyzeResult selectResultByRIDandSID(Long redisInfoId, Long scheduleId) {
        return null;
    }

    @Override
    public List<RDBAnalyzeResult> selectByMap(Map<String, Object> map) {
        return null;
    }

    @Override
    public RDBAnalyzeResult reportDataWriteToDb(RDBAnalyze rdbAnalyze, Map<String, Set<String>> data) {
        return null;
    }

    @Override
    public List<JSONObject> getAllKeyPrefixByResult(String result) {
        return null;
    }

    @Override
    public Object getLineStringFromResult(Long id, Long scheduleId, String key) {
        return null;
    }

    @Override
    public JSONArray getJsonArrayFromResult(Long id, Long scheduleId, String key) {
        return null;
    }

    @Override
    public Object getListStringFromResult(Long id, Long scheduleId, String key) throws Exception {
        return null;
    }

    @Override
    public JSONArray getJSONArrayFromResultByKey(String result, String key) {
        return null;
    }

    @Override
    public Object getTopKeyFromResultByKey(Long id, Long scheduleId, Long key) throws Exception {
        return null;
    }

    @Override
    public JSONArray getTopKeyFromResultByKey(String result, Long startNum) {
        return null;
    }

    @Override
    public JSONArray getPrefixLineByCountOrMem(Long pid, String type, int top, String prefixKey) {
        return null;
    }

    @Override
    public Map<String, JSONObject> getMapJSONByResult(RDBAnalyzeResult rdbAnalyzeResult, JSONArray arrayResult) {
        return null;
    }

    @Override
    public List<String> getcolumnKeyList(String prefixKey, List<JSONObject> resultObjecList, String columnName,
                                         int top) {
        return null;
    }

    @Override
    public List<JSONObject> getJSONObjList(JSONArray jsonArray) {
        return null;
    }

    @Override
    public String getSortColumn(String type) {
        return null;
    }

    @Override
    public JSONArray getPrefixType(Long id, Long scheduleId) throws Exception {
        return null;
    }

    @Override
    public JSONArray getPrefixArrayAddMem(JSONArray count, Map<String, JSONObject> memMap, String column) {
        return null;
    }

    @Override
    public Map<String, JSONObject> getJsonObject(JSONArray jsonArray) {
        return null;
    }

    @Override
    public Map<String, ReportData> getReportDataLatest(Long pid) {
        return null;
    }

    @Override
    public void createRdbAnalyzeResultTable() {
        iRdbAnalyzeResult.createRdbAnalyzeResult();
    }
}
