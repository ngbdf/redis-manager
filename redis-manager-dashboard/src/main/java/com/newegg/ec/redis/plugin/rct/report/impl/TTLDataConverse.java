package com.newegg.ec.redis.plugin.rct.report.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.ExcelData;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.plugin.rct.report.IAnalyzeDataConverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author kz37
 * @date 2018/10/23
 */
public class TTLDataConverse implements IAnalyzeDataConverse {

    private final static Logger LOG = LoggerFactory.getLogger(TTLDataConverse.class);

    @Override
    public Map<String, List<ExcelData>> getPrefixAnalyzerData(Set<String> newSetData, Map<String, ReportData> latestPrefixData) {
        return getExcelDataBytes(newSetData);
    }

    public Map<String, List<ExcelData>> getExcelDataBytes(Set<String> newSetData) {
        Map<String, List<ExcelData>> mapResult = new HashMap<>(2);
        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
        mapResult.putAll(getSheetBodyData(reportDataMap));
        return mapResult;
    }
    @Override
    public Map<String, String> getMapJsonString(Set<String> newSetData) {
        Map<String, String> mapResult = new HashMap<>(2);
        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
        Map<String, List<ExcelData>> excelDataMap = getSheetBodyData(reportDataMap);
        List<JSONObject> dataType = new ArrayList<>();
        JSONObject jsonObject;
        try {
            List<ExcelData> excelDataList = excelDataMap.get(getSheetName());
            for(ExcelData excelData : excelDataList) {
                for(List<String> data : excelData.getTableData()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("prefix", data.get(0));
                    jsonObject.put("noTTL", data.get(1));
                    jsonObject.put("TTL", data.get(2));
                    dataType.add(jsonObject);
                }
            }
            mapResult.put(getSheetName(), JSON.toJSONString(dataType));
        } catch (Exception e) {
            LOG.error("TTLDataConverse getMapJsonString faile");
        }
        return mapResult;
    }

    private Map<String, List<ExcelData>> getSheetBodyData(Map<String, ReportData> reportDataMap) {
        Map<String, List<ExcelData>> resultMap = new HashMap<>(7);
        List<List<String>> sheetData = new ArrayList<List<String>>(10);
        List<String> oneRow = null;
        for(Map.Entry<String, ReportData> entry : reportDataMap.entrySet()) {
            oneRow = converseReportDataToList(entry.getValue());
            sheetData.add(oneRow);
        }
        ExcelData excelData = new ExcelData();
        excelData.setStartColumn(0);
        excelData.setTableData(sheetData);
        List<ExcelData> excelListData = new ArrayList<>();
        excelListData.add(excelData);
        resultMap.put(getSheetName(), excelListData);
        return resultMap;
    }

    private List<String> converseReportDataToList(ReportData reportData) {
        List<String> result = new ArrayList<>(5);
        result.add(reportData.getKey());
        result.add(String.valueOf(reportData.getCount()));
        result.add(String.valueOf(reportData.getBytes()));
        return result;
    }

    private Map<String, ReportData> toReportDataMap(Set<String> newSetData){
        Map<String, ReportData> reportDataHashMap = new HashMap<String, ReportData>(100);
        if(null != newSetData) {
            JSONObject jsonObject = null;
            String prefix = null;
            ReportData reportData = null;
            for(String object : newSetData) {
                jsonObject = JSON.parseObject(object);
                prefix = jsonObject.getString("prefix");
                reportData = reportDataHashMap.get(prefix);
                if(null == reportData) {
                    reportData = new ReportData();
                    reportData.setCount(Long.parseLong(jsonObject.getString("noTTL")));
                    reportData.setBytes(Long.parseLong(jsonObject.getString("TTL")));
                    reportData.setKey(prefix);
                }
                else {
                    reportData.setCount(Long.parseLong(jsonObject.getString("noTTL")) + reportData.getCount());
                    reportData.setBytes(Long.parseLong(jsonObject.getString("TTL")) + reportData.getBytes());
                }
                reportDataHashMap.put(prefix, reportData);
            }
        }
        return reportDataHashMap;
    }

    private String getSheetName() {
        return IAnalyzeDataConverse.TTL_ANALYZE;
    }
}
