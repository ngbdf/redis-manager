package com.newegg.ec.redis.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import com.newegg.ec.redis.dao.IRdbAnalyzeResult;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyzeResult;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.plugin.rct.cache.AppCache;
import com.newegg.ec.redis.plugin.rct.report.IAnalyzeDataConverse;
import com.newegg.ec.redis.plugin.rct.report.converseFactory.ReportDataConverseFacotry;
import com.newegg.ec.redis.service.IRdbAnalyzeResultService;
import com.newegg.ec.redis.util.ListSortUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class RdbAnalyzeResultService implements IRdbAnalyzeResultService {
    @Autowired
    IRdbAnalyzeResult rdbAnalyzeResultMapper;

    private static final Logger LOG = LoggerFactory.getLogger(RdbAnalyzeResultService.class);
    @Override
    public void delete(Long id) {
        rdbAnalyzeResultMapper.deleteById(id);
    }

    @Override
    public void add(RDBAnalyzeResult rdbAnalyzeResult) {
        rdbAnalyzeResultMapper.insert(rdbAnalyzeResult);
    }

    @Override
    public List<RDBAnalyzeResult> selectList(Long groupId) {
        return rdbAnalyzeResultMapper.selectList(groupId);
    }

    /**
     * get result list by resultId
     *
     * @param resultId resultId
     * @return List<RDBAnalyzeResult>
     */
    @Override
    public List<RDBAnalyzeResult> selectAllRecentlyResultById(Long resultId) {
        if (null == resultId) {
            return null;
        }
        List<RDBAnalyzeResult> results = new ArrayList<>(7);
        try {
            RDBAnalyzeResult analyzeResult = rdbAnalyzeResultMapper.selectByResultId(resultId);
            results.add(analyzeResult);
            Map<String, Long> dataMap = new HashMap<String, Long>();
            dataMap.put("resultId", resultId);
            dataMap.put("clusterId", analyzeResult.getClusterId());
            dataMap.put("scheduleId", analyzeResult.getScheduleId());
            List<RDBAnalyzeResult> rDBAnalyzeResults = rdbAnalyzeResultMapper.selectRecentlyResultByIdExceptSelf(dataMap);
            results.addAll(rDBAnalyzeResults);
        } catch (Exception e) {
            LOG.error("selectAllResultById failed!", e);
        }
        return results;
    }

    /**
     * get result list by resultId
     *
     * @param resultId resultId
     * @return List<RDBAnalyzeResult>
     */
    private List<RDBAnalyzeResult> selectRecentlyResultByIdExceptSelf(Long resultId, Long clusterId, Long scheduleId) {
        if (null == resultId) {
            return null;
        }
        List<RDBAnalyzeResult> results = null;
        try {
            Map<String, Long> dataMap = new HashMap<String, Long>();
            dataMap.put("resultId", resultId);
            dataMap.put("clusterId", clusterId);
            dataMap.put("scheduleId", scheduleId);
            results = rdbAnalyzeResultMapper.selectRecentlyResultByIdExceptSelf(dataMap);
        } catch (Exception e) {
            LOG.error("selectAllResultById failed!", e);
        }
        return results;
    }

    /**
     * get result by cluster_id
     *
     * @param clusterId cluster_id
     * @return RDBAnalyzeResult 不包含 cluster_id 和 scheduleId
     */
    private RDBAnalyzeResult selectLatestResultByRID(Long clusterId) {
        if (null == clusterId) {
            return null;
        }
        RDBAnalyzeResult result = null;
        try {
            result = rdbAnalyzeResultMapper.selectLatestResultByRedisInfoId(clusterId);
        } catch (Exception e) {
            LOG.error("selectLatestResultByRedisInfoId failed!", e);
        }
        return result;
    }

    public RDBAnalyzeResult selectResultById(Long id) {
        if (null == id) {
            return null;
        }
        RDBAnalyzeResult result = null;
        try {
            result = rdbAnalyzeResultMapper.selectByResultId(id);
        } catch (Exception e) {
            LOG.error("selectLatestResultByRedisInfoId failed!", e);
        }
        return result;
    }


    /**
     * 插入数据库，并将结果返回
     *
     * @param rdbAnalyze
     * @param data
     * @return
     */
    @Override
    public RDBAnalyzeResult reportDataWriteToDb(RDBAnalyze rdbAnalyze, Map<String, Set<String>> data) {
        try {
            Long scheduleId = AppCache.scheduleProcess.get(rdbAnalyze.getId());
            Long redisClusterId = rdbAnalyze.getClusterId();
            IAnalyzeDataConverse analyzeDataConverse = null;
            Map<String, String> dbResult = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : data.entrySet()) {
                analyzeDataConverse = ReportDataConverseFacotry.getReportDataConverse(entry.getKey());
                if (null != analyzeDataConverse) {
                    dbResult.putAll(analyzeDataConverse.getMapJsonString(entry.getValue()));

                }
            }
            Map<String, String> finalDbResult = combinePrefixKey(dbResult);
            String result = JSON.toJSONString(finalDbResult);
            RDBAnalyzeResult rdbAnalyzeResult = null;
            rdbAnalyzeResult = rdbAnalyzeResultMapper.selectByRedisIdAndSId(redisClusterId, scheduleId);
            rdbAnalyzeResult.setResult(result);
            rdbAnalyzeResult.setDone(true);
            rdbAnalyzeResultMapper.updateResult(rdbAnalyzeResult);
            return rdbAnalyzeResult;
        } catch (Exception e) {
            LOG.error("reportDataWriteToDb write to db error!", e);
        }
        return null;
    }

    public Map<String, String> combinePrefixKey(Map<String, String> dbResult){
        Map<String, String> map = new HashMap<>();
        if(CollectionUtils.isEmpty(dbResult)){
            return map;
        }
        String[] analyzerArr = new String[]{"TTLAnalyze","PrefixKeyByMemory","PrefixKeyByCount"};
        for (String analyzer :analyzerArr){
            String analyzerStr = dbResult.getOrDefault(analyzer,null);
            if(Objects.nonNull(analyzerStr)){
                map.put(analyzer,combinePrefixKeyByType(analyzerStr,analyzer));
            }
        }
        dbResult.putAll(map);
        return dbResult;
    }

    private String combinePrefixKeyByType(String result,String type){
        Map<String, String> map = new HashMap<>();
        if(Objects.isNull(result)){
            return null;
        }
        List<PrefixResult> prefixKeyList =  JSONObject.parseArray(result,PrefixResult.class);
        List<PrefixResult> prefixResultList = combinePrefixByType(prefixKeyList,type);
        List<PrefixResult> finalPrefixResultList = prefixResultList.stream().map(prefixResult -> setPrefixKey(prefixResult,true)).collect(Collectors.toList());
        TypeUtils.compatibleWithJavaBean = true;
        return JSONObject.toJSONString(finalPrefixResultList, SerializerFeature.NotWriteDefaultValue);
    }

    private List<PrefixResult> combinePrefixByType(List<PrefixResult> prefixResultList,String type){
        Map<String,PrefixResult> combinedResult = new TreeMap<>();
        Map<String,PrefixResult> prefixResultMap = listToSortTreeMap(prefixResultList);
        prefixResultList = new ArrayList<>(prefixResultMap.values());
        int i = 0;
        while (i<prefixResultList.size()-1){
            PrefixResult result = prefixResultList.get(i);
            PrefixResult nextResult = prefixResultList.get(i+1);
            if(getPrefix(nextResult).startsWith(getPrefix(result))){
                if("TTLAnalyze".equalsIgnoreCase(type)){
                    int ttl = nextResult.getTTL()+result.getTTL();
                    int noTTL = nextResult.getNoTTL()+result.getNoTTL();
                    result.setTTL(ttl);
                    result.setNoTTL(noTTL);
                }
                if("PrefixKeyByMemory".equalsIgnoreCase(type)){
                    long memorySize = nextResult.getMemorySize()+result.getMemorySize();
                    result.setMemorySize(memorySize);
                }
                if("PrefixKeyByCount".equalsIgnoreCase(type)){
                    long keyCount = nextResult.getKeyCount()+result.getKeyCount();
                    result.setKeyCount(keyCount);
                }
                prefixResultList.remove(nextResult);
            }else {
                i = i+1;
            }
            combinedResult.put(getPrefix(result),result);
        }

        return new ArrayList<>(combinedResult.values());
    }


    private Map<String,PrefixResult> listToSortTreeMap(List<PrefixResult> prefixResultList){
        Map<String,PrefixResult> prefixResultMap = new TreeMap<>();
        prefixResultList.forEach(prefixResult-> {
            prefixResultMap.put(getPrefix(prefixResult), setPrefixKey(prefixResult,false));
        });
        return prefixResultMap;
    }

    private PrefixResult setPrefixKey(PrefixResult prefixResult,boolean addStart){
        if (Objects.nonNull(prefixResult.getPrefixKey())) {
            String prefix = prefixResult.getPrefixKey().replace("*", "");
            prefix = addStart?prefix+"*":prefix;
            prefixResult.setPrefixKey(prefix);
        }
        if (Objects.nonNull(prefixResult.getPrefix())) {
            String prefix = prefixResult.getPrefix().replace("*", "");
            prefix = addStart?prefix+"*":prefix;
            prefixResult.setPrefix(prefix);
        }
        return prefixResult;
    }

    private String getPrefix(PrefixResult result){
        if(Objects.isNull(result.getPrefixKey())){
            return result.getPrefix().endsWith("*")?result.getPrefix().replace("\\*",""):result.getPrefix();
        }
        return result.getPrefixKey().endsWith("*")?result.getPrefixKey().replace("\\*",""):result.getPrefixKey();
    }

    /**
     * get all keyPrefix by analyzeResultId
     * @param analyzeResultId id
     * @return List<JSONObject>
     */
    public List<JSONObject> getAllKeyPrefixById(Long analyzeResultId) {
        RDBAnalyzeResult result = selectResultById(analyzeResultId);
        return getAllKeyPrefixByResult(result.getResult());
    }

    /**
     * get list keyPrefix
     * @param result result
     * @return List<String> keyPrefix
     */
    private List<JSONObject> getAllKeyPrefixByResult(String result){
        List<JSONObject> resultJsonObj = new ArrayList<>(500);
        if(null == result || "".equals(result.trim())) {
            return resultJsonObj;
        }
        JSONArray jsonArray = getJSONArrayFromResultByKey(result, IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT);
        if(null == jsonArray) {
            return resultJsonObj;
        }
        JSONObject oneRow;
        JSONObject jsonObject;
        for(Object obj : jsonArray) {
            oneRow = (JSONObject) obj;
            jsonObject = new JSONObject();
            jsonObject.put("value", oneRow.getString("prefixKey"));
            jsonObject.put("label", oneRow.getString("prefixKey"));
            resultJsonObj.add(jsonObject);
        }
        return resultJsonObj;
    }

    private JSONArray getJsonArrayFromResult(Long analyzeResultId, String key) throws Exception {
        if (null == analyzeResultId) {
            throw new Exception("clusterId should not null!");
        }
        RDBAnalyzeResult rdbAnalyzeResult = selectResultById(analyzeResultId);
        return getJSONArrayFromResultByKey(rdbAnalyzeResult.getResult(), key);
    }

    @Override
    public Object getListStringFromResult(Long analyzeResultId, String key) throws Exception {
        return getJsonArrayFromResult(analyzeResultId, key);
    }

    private JSONArray getJSONArrayFromResultByKey(String result, String key) {
        if ((null == result) || ("".equals(result.trim())) || (null == key) || ("".equals(key.trim()))) {
            return null;
        }
        JSONObject resultJsonObj = JSONObject.parseObject(result);

        return JSONObject.parseArray(resultJsonObj.getString(key));
    }

    @Override
    public Object getTopKeyFromResultByKey(Long analyzeResultId, Long type) throws Exception {
        if (null == analyzeResultId || null == type) {
            throw new Exception("id or key should not null!");
        }
        RDBAnalyzeResult rdbAnalyzeResult = selectResultById(analyzeResultId);
        return getTopKeyFromResultByKey(rdbAnalyzeResult.getResult(), type);
    }

    // 获取指定类型的TopKey数据
    private JSONArray getTopKeyFromResultByKey(String result, Long type) {
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJsonObj = JSONObject.parseObject(result);
            if(resultJsonObj != null && resultJsonObj.containsKey(IAnalyzeDataConverse.TOP_KEY_ANALYZE)){
                JSONObject topKeyData = JSONObject.parseObject(resultJsonObj.getString(IAnalyzeDataConverse.TOP_KEY_ANALYZE));
                return topKeyData.getJSONArray(String.valueOf(type));
            }
        }
        return null;
    }

    /**
     * @param analyzeResultId clusterId
     * @param type            PrefixKeyByCount,PrefixKeyByMemory
     * @return JSONArray
     */
    @Override
    public JSONObject getPrefixLineByCountOrMem(Long analyzeResultId, String type, int top, String prefixKey) {
        JSONObject jsonResultObject = new JSONObject();
        String sortColumn = getSortColumn(type);
        // RDBAnalyzeResult rdbAnalyzeLatestResult = selectLatestResultByRID(clusterId);
        RDBAnalyzeResult rdbAnalyzeLatestResult = selectResultById(analyzeResultId);
        if (null == rdbAnalyzeLatestResult) {
            return null;
        }
        JSONArray arrayResult = getJSONArrayFromResultByKey(rdbAnalyzeLatestResult.getResult(), type);
        if (null == arrayResult) {
            return null;
        }
        List<JSONObject> resultObjectList = getJSONObjList(arrayResult);
        ListSortUtil.sortByKeyValueDesc(resultObjectList, sortColumn);
        // top == -1 代表全部，否则截取前 top
        // 需要返回的前缀
        List<String> prefixKeyList = getcolumnKeyList(prefixKey, resultObjectList, "prefixKey", top);
        // except Latest RDBAnalyzeResult
        List<RDBAnalyzeResult> rdbAnalyzeResultList = selectRecentlyResultByIdExceptSelf(analyzeResultId,
                rdbAnalyzeLatestResult.getClusterId(), rdbAnalyzeLatestResult.getScheduleId());
        // 过滤掉非集群模式的分析结果
        rdbAnalyzeResultList = rdbAnalyzeResultList.stream().filter(rdbAnalyzeResult -> {
            JSONObject object = JSONObject.parseObject(rdbAnalyzeResult.getAnalyzeConfig());
            JSONArray array = object.getJSONArray("nodes");
            if (array.size() == 1 && "-1".equals(array.getString(0))) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        // key ：prefixKey
        Map<String, Map<String, JSONObject>> resultMap = new HashMap<>(7);
        Map<String, JSONObject> latest = getMapJSONByResult(rdbAnalyzeLatestResult, arrayResult);
        resultMap.put(String.valueOf(rdbAnalyzeLatestResult.getScheduleId()), latest);
        // scheduleList 列表
        List<Long> scheduleList = new ArrayList<>(7);
        scheduleList.add(rdbAnalyzeLatestResult.getScheduleId());
        for (RDBAnalyzeResult rdbAnalyzeResult : rdbAnalyzeResultList) {
            if (null != rdbAnalyzeResult.getResult()) {
                arrayResult = getJSONArrayFromResultByKey(rdbAnalyzeResult.getResult(), type);
                resultMap.put(String.valueOf(rdbAnalyzeResult.getScheduleId()), getMapJSONByResult(rdbAnalyzeResult, arrayResult));
                scheduleList.add(rdbAnalyzeResult.getScheduleId());
            }
        }
        Collections.sort(scheduleList);
        JSONArray result = new JSONArray();
        JSONObject arrayJsonObj;
        String id;
        Map<String, JSONObject> res;
        StringBuilder sb;
        JSONObject temp;
        String value;
        for (String prefix : prefixKeyList) {
            arrayJsonObj = new JSONObject();
            sb = new StringBuilder();
            for (Long schedule : scheduleList) {
                id = String.valueOf(schedule);
                res = resultMap.get(id);
                temp = res.get(prefix);
                if (null == temp) {
                    // 没有对应前缀，值为 0
                    value = "0";
                } else {
                    value = temp.getString(sortColumn);
                }
                sb.append(value).append(",");
            }
            arrayJsonObj.put("value", sb.toString().substring(0, sb.toString().length() - 1));
            arrayJsonObj.put("key", prefix);
            result.add(arrayJsonObj);
        }
        jsonResultObject.put("time", scheduleList);
        jsonResultObject.put("data", result);
        return jsonResultObject;

    }

    /**
     * 将数据中结果转换为 折线图需要的对象
     *
     * @param rdbAnalyzeResult
     * @return
     */
    private Map<String, JSONObject> getMapJSONByResult(RDBAnalyzeResult rdbAnalyzeResult, JSONArray arrayResult) {
        Map<String, JSONObject> result = new HashMap<>(500);
        Long scheduleId = rdbAnalyzeResult.getScheduleId();
        JSONObject object;
        for (Object obj : arrayResult) {
            object = (JSONObject) obj;
            object.put("scheduleId", scheduleId);
            result.put(object.getString("prefixKey"), object);
        }
        JSONObject scheduleIdJson = new JSONObject();
        scheduleIdJson.put("scheduleId", scheduleId);
        result.put("scheduleId", scheduleIdJson);
        return result;
    }

    /**
     * @param prefixKey       固定前缀
     * @param resultObjecList result
     * @param columnName      获取的列
     * @param top             top
     * @return
     */
    private List<String> getcolumnKeyList(String prefixKey, List<JSONObject> resultObjecList, String columnName,
                                          int top) {
        List<String> prefixKeyList = new ArrayList<>(10);
        if (null == prefixKey || "".equals(prefixKey)) {
            if (top == -1) {
                top = resultObjecList.size();
            }
            int i = 0;
            for (JSONObject tempObj : resultObjecList) {
                if (i >= top) {
                    break;
                }
                prefixKeyList.add(tempObj.getString(columnName));
                i++;
            }
        } else {
            prefixKeyList.add(prefixKey);
        }
        return prefixKeyList;
    }

    /**
     * 将 jsonArray 转换为 List<JSONObject>
     *
     * @param jsonArray array
     * @return List<JSONObject>
     */
    private List<JSONObject> getJSONObjList(JSONArray jsonArray) {
        List<JSONObject> resultList = new ArrayList<>(300);
        JSONObject jsonObject;
        for (Object obj : jsonArray) {
            jsonObject = (JSONObject) obj;
            resultList.add(jsonObject);
        }
        return resultList;
    }

    private String getSortColumn(String type) {
        String column;
        if (IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT.equalsIgnoreCase(type)) {
            column = "keyCount";
        } else if (IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY.equalsIgnoreCase(type)) {
            column = "memorySize";
        } else {
            column = null;
        }
        return column;
    }


    @Override
    public JSONArray getPrefixType(Long analyzeResultId) throws Exception {
        if (null == analyzeResultId) {
            throw new Exception("analyzeResultId should not null!");
        }
        JSONArray count = getJsonArrayFromResult(analyzeResultId, IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT);
        JSONArray memory = getJsonArrayFromResult(analyzeResultId, IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY);
        if (null == memory || memory.isEmpty()) {
            return count;
        }
        Map<String, JSONObject> memMap = getJsonObject(memory);
        return getPrefixArrayAddMem(count, memMap, "memorySize");
    }


    private JSONArray getPrefixArrayAddMem(JSONArray count, Map<String, JSONObject> memMap, String column) {
        JSONArray result = new JSONArray();
        JSONObject jsonObject;
        JSONObject temp;
        String prefix;
        for (Object obj : count) {
            jsonObject = (JSONObject) obj;
            prefix = jsonObject.getString("prefixKey");
            temp = memMap.get(prefix);
            if (null != temp) {
                jsonObject.put(column, temp.getString(column));
            }
            result.add(jsonObject);
        }
        return result;
    }


    private Map<String, JSONObject> getJsonObject(JSONArray jsonArray) {
        Map<String, JSONObject> result = new HashMap<>(280);
        JSONObject temp;
        for (Object obj : jsonArray) {
            temp = (JSONObject) obj;
            result.put(temp.getString("prefixKey"), temp);
        }
        return result;
    }


    /**
     * 获取上一次的数据转换为ReportData
     *
     * @param clusterId clusterId
     * @return Map
     */
    @Override
    public Map<String, ReportData> getReportDataLatest(Long clusterId) {
        if (null == clusterId) {
            return null;
        }
        RDBAnalyzeResult rdbAnalyzeResult = selectLatestResultByRID(clusterId);
        if (null == rdbAnalyzeResult) {
            return null;
        }
        JSONArray countResult = getJSONArrayFromResultByKey(rdbAnalyzeResult.getResult(), IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT);
        JSONArray memResult = getJSONArrayFromResultByKey(rdbAnalyzeResult.getResult(), IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY);
        return getPrefixReportData(countResult, memResult);
    }

    /**
     * 数据转换
     *
     * @param arrayResult
     * @return
     */
    private Map<String, JSONObject> getMapJsonPrefixByResult(JSONArray arrayResult) {
        Map<String, JSONObject> result = new HashMap<>(500);
        JSONObject object;
        if (null != arrayResult && !arrayResult.isEmpty()) {
            for (Object obj : arrayResult) {
                object = (JSONObject) obj;
                result.put(object.getString("prefixKey"), object);
            }
        }
        return result;
    }

    private Map<String, ReportData> getPrefixReportData(JSONArray countResult, JSONArray memResult) {
        if (null == countResult || countResult.isEmpty() || null == memResult || memResult.isEmpty()) {
            return null;
        }
        Map<String, JSONObject> memResultJsonObj = getMapJsonPrefixByResult(memResult);
        JSONObject temp;
        Map<String, ReportData> result = new HashMap<>(667);
        ReportData reportData;
        String prefix;
        for (Object obj : countResult) {
            temp = (JSONObject) obj;
            prefix = temp.getString("prefixKey");
            reportData = new ReportData();
            reportData.setKey(prefix);
            reportData.setCount(temp.getLongValue("keyCount"));
            JSONObject mem = memResultJsonObj.get(prefix);
            if (null == mem) {
                reportData.setBytes(0L);
            } else {
                reportData.setBytes(mem.getLongValue("memorySize"));
            }
            result.put(prefix, reportData);
        }
        return result;

    }

    @Override
    public void createRdbAnalyzeResultTable() {
        rdbAnalyzeResultMapper.createRdbAnalyzeResult();
    }

    /**
     * 获取所有的分析结果，并设置clusterName
     *
     * @param clusters all cluster
     * @param results  all result
     * @return
     */
    @Override
    public List<RDBAnalyzeResult> getAllAnalyzeResult(List<RDBAnalyzeResult> results, List<Cluster> clusters) {
        Map<Integer, String> clusterName = clusters.stream().collect(Collectors.toMap(Cluster::getClusterId,
                Cluster::getClusterName));
        for (RDBAnalyzeResult result : results) {
            result.setClusterName(clusterName.get(result.getClusterId().intValue()));
        }
        return results;
    }

    static class PrefixResult{
        private String prefixKey;
        private int noTTL = 0;

        private int TTL = 0 ;
        private long memorySize;
        private long keyCount;
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefixKey() {
            return prefixKey;
        }

        public void setPrefixKey(String prefixKey) {
            this.prefixKey = prefixKey;
        }

        public int getNoTTL() {
            return noTTL;
        }

        public void setNoTTL(int noTTL) {
            this.noTTL = noTTL;
        }

        public int getTTL() {
            return TTL;
        }

        public void setTTL(int TTL) {
            this.TTL = TTL;
        }

        public long getMemorySize() {
            return memorySize;
        }

        public void setMemorySize(long memorySize) {
            this.memorySize = memorySize;
        }

        public long getKeyCount() {
            return keyCount;
        }

        public void setKeyCount(long keyCount) {
            this.keyCount = keyCount;
        }
    }
}
