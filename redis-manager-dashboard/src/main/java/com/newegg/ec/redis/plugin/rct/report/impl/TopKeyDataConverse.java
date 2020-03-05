package com.newegg.ec.redis.plugin.rct.report.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.ExcelData;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.entity.TopKeyReportData;
import com.newegg.ec.redis.plugin.rct.report.IAnalyzeDataConverse;
import com.newegg.ec.redis.util.BytesConverseUtil;
import com.newegg.ec.redis.util.ListSortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author kz37
 * @date 2018/10/23
 */
public class TopKeyDataConverse implements IAnalyzeDataConverse {

    private final static Logger LOG = LoggerFactory.getLogger(TopKeyDataConverse.class);

    @Override
    public Map<String, List<ExcelData>> getPrefixAnalyzerData(Set<String> newSetData, Map<String, ReportData> latestPrefixData) {
    	Map<String, List<ExcelData>> mapResult = getExcelDataBytes(newSetData);
        List<ExcelData> excelDataList = mapResult.get(getSheetName());
        for(ExcelData excelData : excelDataList) {
            //对 list的第三个值做转换，List key，dataType，bytes，itemCount
            BytesConverseUtil.converseBytes(excelData.getTableData(), 2);
        }
        return mapResult;
    }

    public Map<String, List<ExcelData>> getExcelDataBytes(Set<String> newSetData) {
        Map<String, List<ExcelData>> mapResult = new HashMap<>();
        try {
            Map<String, TopKeyReportData> reportDataMap = toReportDataMap(newSetData);
            mapResult.putAll(getSheetBodyData(reportDataMap));
            return mapResult;
        } catch (Exception e) {
            LOG.error("TopKeyDataConverse getPrefixAnalyzerData faile");
        }
        return mapResult;
    }

    /**
     * 将数据转换为json
     * @param newSetData
     * @return Map : key: sheetName, value:resultString
     *
     * TopKeyAnalyze, 0 5 10 15
     */
    @Override
    public Map<String, String> getMapJsonString(Set<String> newSetData) {
        Map<String, String> mapResult = new HashMap<>(2);
        try {
            Map<String, TopKeyReportData> reportDataMap = toReportDataMap(newSetData);
            List<ExcelData> excelDataList = getBodyDataList(reportDataMap);
            // key: startColumn
            Map<String, List<JSONObject>> oneDataType = new HashMap<>(5);
            List<JSONObject> dataType;
            JSONObject jsonObject;
            for(ExcelData excelData : excelDataList) {
                dataType = new ArrayList<>(excelDataList.size());
                for(List<String> data : excelData.getTableData()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("key", data.get(0));
                    jsonObject.put("dataType", data.get(1));
                    jsonObject.put("bytes", data.get(2));
                    jsonObject.put("itemCount", data.get(3));
                    dataType.add(jsonObject);
                }
                oneDataType.put(String.valueOf(excelData.getStartColumn()),dataType);
            }
            mapResult.put(getSheetName(), JSON.toJSONString(oneDataType));
        } catch (Exception e) {
            LOG.error("TopKeyDataConverse getMapJsonString faile");
        }
        return mapResult;
    }

    private Map<String, List<ExcelData>> getSheetBodyData(Map<String, TopKeyReportData> reportDataMap) {
        Map<String, List<ExcelData>> resultMap = new HashMap<>(7);
        List<ExcelData> excelDatas = getBodyDataList(reportDataMap);
//        for(ExcelData excelData : excelDatas) {
//            BytesConverseUtil.converseBytes(excelData.getTableData(), 2);
//        }
        resultMap.put(getSheetName(), excelDatas);
        return resultMap;
    }

    private List<ExcelData> getBodyDataList(Map<String, TopKeyReportData> reportDataMap) {
        List<List<String>> rowStringData = new ArrayList<List<String>>(1000);
        List<List<String>> rowListData = new ArrayList<List<String>>(1000);
        List<List<String>> rowHashData = new ArrayList<List<String>>(1000);
        List<List<String>> rowSetData = new ArrayList<List<String>>(1000);
        List<String> rowString = null;
        List<String> rowList = null;
        List<String> rowHash = null;
        List<String> rowSet = null;
        TopKeyReportData reportData = null;
        for(Map.Entry<String, TopKeyReportData> entry : reportDataMap.entrySet()) {
            reportData = entry.getValue();
            if(reportData.getDataType().equals("String")) {
                rowString = converseReportDataToList(reportData);
                rowStringData.add(rowString);
            } else if(reportData.getDataType().equals("List")) {
                rowList = converseReportDataToList(reportData);
                rowListData.add(rowList);
            } else if(reportData.getDataType().equals("Hash")) {
                rowHash = converseReportDataToList(reportData);
                rowHashData.add(rowHash);
            } else if(reportData.getDataType().equals("Set")) {
                rowSet = converseReportDataToList(reportData);
                rowSetData.add(rowSet);
            } else {
                continue;
            }
        }
        ListSortUtil.sortListListStringDesc(rowStringData, 2);
        ListSortUtil.sortListListStringDesc(rowHashData, 2);
        ListSortUtil.sortListListStringDesc(rowListData, 2);
        ListSortUtil.sortListListStringDesc(rowSetData, 2);
//        BytesConverseUtil.converseBytes(rowStringData, 2);
//        BytesConverseUtil.converseBytes(rowHashData, 2);
//        BytesConverseUtil.converseBytes(rowListData, 2);
//        BytesConverseUtil.converseBytes(rowSetData, 2);
        ExcelData stringExcelData = new ExcelData(TOP_KEY_STRING_DATA, rowStringData.subList(0, rowStringData.size() > 1000 ? 1000 : rowStringData.size()));
        ExcelData hashExcelData = new ExcelData(TOP_KEY_HASH_DATA, rowHashData.subList(0, rowHashData.size() > 1000 ? 1000 : rowHashData.size()));
        ExcelData listExcelData = new ExcelData(TOP_KEY_LIST_DATA, rowListData.subList(0, rowListData.size() > 1000 ? 1000 : rowListData.size()));
        ExcelData setExcelData = new ExcelData(TOP_KEY_SET_DATA, rowSetData.subList(0, rowSetData.size() > 1000 ? 1000 : rowSetData.size()));
        List<ExcelData> excelData = new ArrayList<>(4);
        excelData.add(stringExcelData);
        excelData.add(hashExcelData);
        excelData.add(listExcelData);
        excelData.add(setExcelData);
        return excelData;
    }

    private List<String> converseReportDataToList(TopKeyReportData reportData) {
        List<String> result = new ArrayList<>(5);
        result.add(reportData.getKey());
        result.add(reportData.getDataType());
        result.add(String.valueOf(reportData.getBytes()));
        result.add(String.valueOf(reportData.getCount()));
        return result;
    }

    private Map<String, TopKeyReportData> toReportDataMap(Set<String> newSetData){
        Map<String, TopKeyReportData> reportDataHashMap = new HashMap<String, TopKeyReportData>(100);
        if(null != newSetData) {
            JSONObject jsonObject = null;
            String prefix = null;
            TopKeyReportData reportData = null;
            for(String object : newSetData) {
                jsonObject = JSON.parseObject(object);
                prefix = jsonObject.getString("key");
                reportData = reportDataHashMap.get(prefix);
                if(null == reportData) {
                    reportData = new TopKeyReportData();
                    reportData.setCount(Long.parseLong(jsonObject.getString("itemCount")));
                    reportData.setBytes(Long.parseLong(jsonObject.getString("bytes")));
                    reportData.setKey(prefix);
                    reportData.setDataType(jsonObject.getString("dataType"));
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
        return IAnalyzeDataConverse.TOP_KEY_ANALYZE;
    }
}
