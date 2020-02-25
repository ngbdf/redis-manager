package com.newegg.ec.redis.plugin.rct.report.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.ExcelData;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.plugin.rct.report.IAnalyzeDataConverse;
import com.newegg.ec.redis.util.BytesConverseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author kz37
 * @date 2018/10/19
 */
public class DataTypeDataConverse implements IAnalyzeDataConverse {

    private final static Logger LOG = LoggerFactory.getLogger(DataTypeDataConverse.class);

    @Override
    public Map<String, List<ExcelData>> getPrefixAnalyzerData(Set<String> newSetData, Map<String, ReportData> latestPrefixData) {
        Map<String, List<ExcelData>> mapResult = new HashMap<>();
        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
        List<ExcelData> excelDataList = getSheetBodyDataBytes(reportDataMap);
        for(ExcelData excelData : excelDataList) {
            //对 list的第三个值做转换，List dataType，itemCount，bytes
            BytesConverseUtil.converseBytes(excelData.getTableData(), 2);
        }
        mapResult.put(getSheetName(), excelDataList);
        return mapResult;
    }
    @Override
    public Map<String, String> getMapJsonString(Set<String> newSetData) {
        Map<String, String> mapResult = new HashMap<>(2);
        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
        List<JSONObject> dataType = new ArrayList<>();
        JSONObject jsonObject;
        try {
            List<ExcelData> excelDataList = getSheetBodyDataBytes(reportDataMap);
                for(ExcelData excelData : excelDataList) {
                    for(List<String> data : excelData.getTableData()) {
                        jsonObject = new JSONObject();
                        jsonObject.put("dataType", data.get(0));
                        jsonObject.put("itemCount", data.get(1));
                        jsonObject.put("bytes", data.get(2));
                        dataType.add(jsonObject);
                    }
                }
                mapResult.put(getSheetName(), JSON.toJSONString(dataType));
        } catch (Exception e) {
            LOG.error("DataTypeDataConverse getMapJsonString faile");
        }
        return mapResult;
    }



//    private Map<String, List<ExcelData>> getSheetBodyData(Map<String, ReportData> reportDataMap) {
//        Map<String, List<ExcelData>> resultMap = new HashMap<>(7);
//        List<List<String>> sheetData = new ArrayList<List<String>>(10);
//        List<String> oneRow = null;
//        for(Map.Entry<String, ReportData> entry : reportDataMap.entrySet()) {
//            oneRow = converseReportDataToList(entry.getValue());
//            sheetData.add(oneRow);
//        }
//        BytesConverseUtil.converseBytes(sheetData, 2);
//        ExcelData excelData = new ExcelData(0, sheetData);
//        List<ExcelData> data = new ArrayList<>();
//        data.add(excelData);
//        resultMap.put(getSheetName(), data);
//        return resultMap;
//    }

    private List<ExcelData> getSheetBodyDataBytes(Map<String, ReportData> reportDataMap) {
        List<List<String>> sheetData = new ArrayList<List<String>>(10);
        List<String> oneRow = null;
        for(Map.Entry<String, ReportData> entry : reportDataMap.entrySet()) {
            oneRow = converseReportDataToList(entry.getValue());
            sheetData.add(oneRow);
        }
        //BytesConverseUtil.converseBytes(sheetData, 2);
        ExcelData excelData = new ExcelData(0, sheetData);
        List<ExcelData> data = new ArrayList<>();
        data.add(excelData);
        return data;
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
                prefix = jsonObject.getString("dataType");
                reportData = reportDataHashMap.get(prefix);
                if(null == reportData) {
                    reportData = new ReportData();
                    reportData.setBytes(Long.parseLong(jsonObject.getString("bytes")));
                    reportData.setCount(Long.parseLong(jsonObject.getString("itemCount")));
                    reportData.setKey(prefix);
                }
                else {
                    reportData.setCount(Long.parseLong(jsonObject.getString("itemCount")) + reportData.getCount());
                    reportData.setBytes(Long.parseLong(jsonObject.getString("bytes")) + reportData.getBytes());
                }
                reportDataHashMap.put(prefix, reportData);
            }
        }
        return reportDataHashMap;
    }

    private String getSheetName() {
        return IAnalyzeDataConverse.DATA_TYPE_ANALYZE;
    }

    // dataType不需要排序截取
}
