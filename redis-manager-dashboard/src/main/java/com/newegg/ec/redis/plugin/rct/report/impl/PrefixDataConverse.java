package com.newegg.ec.redis.plugin.rct.report.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.ExcelData;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.plugin.rct.report.IAnalyzeDataConverse;
import com.newegg.ec.redis.util.BytesConverseUtil;
import com.newegg.ec.redis.util.ListSortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @ClassName PrefixDataConverseService
 * @Description TODO
 * @Author kz37
 * @Date 2018/10/19
 */
public class PrefixDataConverse implements IAnalyzeDataConverse {

    private final static Logger LOG = LoggerFactory.getLogger(PrefixDataConverse.class);

    private static DecimalFormat df = new DecimalFormat("0.0000");

    /**
     *  转换数据
     * @param newSetData '{"prefix":"kyle","itemCount":"2","bytes":"1056"}'
     * @param oldSetData
     * @return
     */
    @Override
    public Map<String, List<ExcelData>> getPrefixAnalyzerData(Set<String> newSetData, Map<String, ReportData> latestPrefixData) {

        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
        //计算增长率
        if(null != latestPrefixData) {
            setGrowthRate(reportDataMap, latestPrefixData);
        }
        Map<String, List<ExcelData>> mapResult = getPrefixAnalyzerBodyData(reportDataMap);
        List<ExcelData> excelDataList = mapResult.get(getMemorySheetName());
        for(ExcelData excelData : excelDataList) {
            //对 list的第2个值做转换，List prefixKey,Memory Size,
            BytesConverseUtil.converseBytes(excelData.getTableData(), 1);
        }
        return mapResult;
    }

//    @Override
//    public Map<String, List<ExcelData>> getExcelDataBytes(Set<String> newSetData) {
//        Map<String, List<ExcelData>> mapResult = new HashMap<>(2);
//        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
//        mapResult.putAll(getPrefixAnalyzerBodyData(reportDataMap));
//        return mapResult;
//    }

    @Override
    public Map<String, String> getMapJsonString(Set<String> newSetData) {
        Map<String, String> mapResult = new HashMap<>(2);
        Map<String, ReportData> reportDataMap = toReportDataMap(newSetData);
        Map<String, List<ExcelData>> excelDataMap = getPrefixAnalyzerBodyData(reportDataMap);
        List<JSONObject> dataType = new ArrayList<>();
        JSONObject jsonObject;
        try {
            // prefix count
            List<ExcelData> excelDataCountList = excelDataMap.get(getCountSheetName());
            for(ExcelData excelData : excelDataCountList) {
                for(List<String> data : excelData.getTableData()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("prefixKey", data.get(0));
                    jsonObject.put("keyCount", data.get(1));
                    dataType.add(jsonObject);
                }
            }
            mapResult.put(getCountSheetName(), JSON.toJSONString(dataType));
            dataType = new ArrayList<>();
            // prefix memory
            List<ExcelData> excelDataMemList = excelDataMap.get(getMemorySheetName());
            for(ExcelData excelData : excelDataMemList) {
                for(List<String> data : excelData.getTableData()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("prefixKey", data.get(0));
                    jsonObject.put("memorySize", data.get(1));
                    dataType.add(jsonObject);
                }
            }
            mapResult.put(getMemorySheetName(), JSON.toJSONString(dataType));
        } catch (Exception e) {
            LOG.error("PrefixDataConverse getMapJsonString faile");
        }
        return mapResult;
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
                    reportData.setCount(Long.parseLong(jsonObject.getString("itemCount")));
                    reportData.setBytes(Long.parseLong(jsonObject.getString("bytes")));
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

    private Map<String, ReportData> setGrowthRate(Map<String, ReportData> newReportDataMap, Map<String, ReportData> oldReportDataMap) {
        ReportData oldReportData = null;
        ReportData newReportData = null;
        for(Map.Entry<String, ReportData> entry : oldReportDataMap.entrySet()) {
            oldReportData = entry.getValue();
            newReportData = newReportDataMap.get(oldReportData.getKey());
            if(null != newReportData) {
                newReportData.setCountGrowthRate(calculateGrowthRate(newReportData.getCount(), oldReportData.getCount()));
                newReportData.setBytesGrowthRate(calculateGrowthRate(newReportData.getBytes(), oldReportData.getBytes()));
            }
        }
        return newReportDataMap;
    }

    private Map<String, List<ExcelData>> getPrefixAnalyzerBodyData(Map<String, ReportData> reportDataMap) {
        Map<String, List<ExcelData>> resultMap = new HashMap<>(5);
        List<List<String>> countSheetData = new ArrayList<List<String>>(100);
        List<List<String>> memorySheetData = new ArrayList<List<String>>(100);
        List<String> oneRow = null;
        for(Map.Entry<String, ReportData> entry : reportDataMap.entrySet()) {
            oneRow = converseReportDataToCountList(entry.getValue());
            countSheetData.add(oneRow);
            oneRow = converseReportDataToMemoryList(entry.getValue());
            memorySheetData.add(oneRow);
        }
        ListSortUtil.sortListListStringDesc(countSheetData, 1);
        ListSortUtil.sortListListStringDesc(memorySheetData, 1);
//        BytesConverseUtil.converseBytes(memorySheetData, 1);
        ExcelData countData = new ExcelData(0, countSheetData);
        ExcelData memoryData = new ExcelData(0, memorySheetData);
        List<ExcelData> countListData = new ArrayList<>();
        countListData.add(countData);
        List<ExcelData> memoryListData = new ArrayList<>();
        memoryListData.add(memoryData);
        resultMap.put(getCountSheetName(), countListData);
        resultMap.put(getMemorySheetName(), memoryListData);
        return resultMap;
    }

    private List<String> converseReportDataToCountList(ReportData reportData) {
        List<String> result = new ArrayList<>(5);
        result.add(reportData.getKey());
        result.add(String.valueOf(reportData.getCount()));
        result.add(df.format(reportData.getCountGrowthRate()));
        return result;
    }

    private List<String> converseReportDataToMemoryList(ReportData reportData) {
        List<String> result = new ArrayList<>(5);
        result.add(reportData.getKey());
        result.add(String.valueOf(reportData.getBytes()));
        result.add(df.format(reportData.getBytesGrowthRate()));
        return result;
    }

    private double calculateGrowthRate(long newData, long oldData) {
        if(oldData == 0) {
            return 0;
        }
        double growthRate = (newData - oldData) * 1.0D / oldData;
        return growthRate;
    }

    private String getCountSheetName() {
        return IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT;
    }

    private String getMemorySheetName() {
        return IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY;
    }
}
