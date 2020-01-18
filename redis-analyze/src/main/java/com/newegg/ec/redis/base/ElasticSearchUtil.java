package com.newegg.ec.redis.base;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Hulva Luva.H
 * @since 2018年4月11日
 *
 */
public class ElasticSearchUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchUtil.class);
	private static PoolingHttpClientConnectionManager cm;
	// 设置超时时间
	private static int REQUEST_TIMEOUT;
	private static int REQUEST_SOCKET_TIME;
	private static String EMPTY_STR = "";
	private static String[] arrUrl;

	static {
		cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
	}

	public static void init(Integer requestTimeout, Integer socketTimeout, String esUrls) {
		REQUEST_TIMEOUT = requestTimeout;
		REQUEST_SOCKET_TIME = socketTimeout;
		arrUrl = esUrls.split(",");
	}

	public static String getUrl() throws Exception {
		if (arrUrl.length > 0) {
			return arrUrl[0];
		} else {
			throw new Exception("errors:now has no es rest url can use !Please to check elasticsearch config");
		}
	}

	// 修改host重连策略
	private static void changeErrorUrl() {
		if (arrUrl.length > 1) {
			String[] tempUrl = new String[arrUrl.length];
			for (int i = 0; i < arrUrl.length - 1; i++) {
				tempUrl[i] = arrUrl[i + 1];
			}
			tempUrl[arrUrl.length - 1] = arrUrl[0];
			arrUrl = tempUrl;
		} else {
			arrUrl = new String[0];
		}
	}

	private static CloseableHttpClient getHttpClient() {

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT)
				.setSocketTimeout(REQUEST_SOCKET_TIME).build();

		return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
	}

	/**
	 * 处理Http请求
	 *
	 * @param request
	 * @return
	 */
	private static String getResult(HttpRequestBase request) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			// int responseCode = response.getStatusLine().getStatusCode();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf-8");
				response.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				String path = request.getURI().getPath();
				request.setURI(URI.create(arrUrl[0] + path));
				return getResult(request);
			} else {
				e.printStackTrace();
			}
		} catch (ConnectTimeoutException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				String path = request.getURI().getPath();
				request.setURI(URI.create(arrUrl[0] + path));
				return getResult(request);
			} else {
				e.printStackTrace();
			}
		} catch (HttpHostConnectException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				String path = request.getURI().getPath();
				request.setURI(URI.create(arrUrl[0] + path));
				return getResult(request);
			} else {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

		return EMPTY_STR;
	}

	public static String httpHead(String url) {
		HttpHead httpHead = new HttpHead(url);
		return getResult(httpHead);
	}

	private static String httpGet(String url) {
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet);
	}

	private static String httpPut(String url, HttpEntity entity) {
		HttpPut httpPut = new HttpPut(url);
		httpPut.setEntity(entity);
		return getResult(httpPut);
	}

	private static String httpDelete(String url) {
		HttpDelete httpDelete = new HttpDelete(url);
		return getResult(httpDelete);
	}

	public static String httpPost(String url, HttpEntity entity) {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		return getResult(httpPost);
	}

	/**
	 * 判断index模板是否存在
	 *
	 * @param templateName
	 * @return true/false
	 * @throws Exception
	 */
	public static boolean templateExists(String templateName) throws Exception {
		HttpHead httpHead = new HttpHead(getUrl() + "/_template/" + templateName);
		CloseableHttpClient httpClient = getHttpClient();
		try {
			CloseableHttpResponse response = httpClient.execute(httpHead);
			int httpCode = response.getStatusLine().getStatusCode();
			response.close();
			if (200 == httpCode) {
				return true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return templateExists(templateName);
			} else {
				e.printStackTrace();
			}
		} catch (ConnectTimeoutException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return templateExists(templateName);
			} else {
				e.printStackTrace();
			}
		} catch (HttpHostConnectException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return templateExists(templateName);
			} else {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return false;
	}

	public static boolean indicesExists(String indexName) throws Exception {
		HttpHead httpHead = new HttpHead(getUrl() + "/" + indexName);
		CloseableHttpClient httpClient = getHttpClient();
		try {
			CloseableHttpResponse response = httpClient.execute(httpHead);
			int httpCode = response.getStatusLine().getStatusCode();
			response.close();
			if (200 == httpCode) {
				return true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return indicesExists(indexName);
			} else {
				e.printStackTrace();
			}
		} catch (ConnectTimeoutException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return indicesExists(indexName);
			} else {
				e.printStackTrace();
			}
		} catch (HttpHostConnectException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return indicesExists(indexName);
			} else {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return false;
	}

	public static boolean createIndex(String indexName, String settings) throws Exception {
		StringEntity entity = new StringEntity(settings, ContentType.APPLICATION_JSON);
		String result = httpPut(getUrl() + "/" + indexName, entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("acknowledged") && json.getBoolean("acknowledged")) {
				return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	public static Set<String> fuzzyGetIndices(String prefixName) {
		Set<String> indices = new HashSet<String>();
		try {
			String result = httpGet(getUrl() + "/" + prefixName + "*");
			JSONObject json = JSONObject.parseObject(result);
			if (!json.isEmpty()) {
				indices = json.keySet();
			}

		} catch (HttpHostConnectException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return fuzzyGetIndices(prefixName);
			} else {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return indices;
	}

	public static boolean deleteIndex(String indexName) throws Exception {
		try {
			String result = httpDelete(getUrl() + "/" + indexName);
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("acknowledged") && json.getBoolean("acknowledged")) {
				return true;
			}
			return false;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean deleteIndex(String index, String type, String id) {
		try {
			String result = httpDelete(getUrl() + "/" + index + "/" + type + "/" + id + "?refresh=true");
			JSONObject json = JSONObject.parseObject(result);
			if ("deleted".equals(json.get("result")) || "not_found".equals(json.get("result"))) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * deleteByQueryIndex
	 *
	 * @param indexName
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public static boolean deleteByQueryIndex(String indexName, String query) throws Exception {
		try {
			StringEntity entity = new StringEntity(query, ContentType.APPLICATION_JSON);
			String result = httpPost(getUrl() + "/" + indexName + "/_delete_by_query", entity);
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("error")) {
				LOG.info("deleteByQueryIndex failed! errors: " + json.toString());
				return false;
			}
			return true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (HttpHostConnectException e) {
			changeErrorUrl();
			if (arrUrl.length > 0) {
				return deleteByQueryIndex(indexName, query);
			} else {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return false;
	}

	/**
	 * 添加 index template
	 *
	 * @param templateName
	 * @param templateData
	 * @return true/false
	 * @throws Exception
	 */
	public static boolean putTemplate(String templateName, String templateData) throws Exception {
		StringEntity entity = new StringEntity(templateData, ContentType.APPLICATION_JSON);
		String result = httpPut(getUrl() + "/_template/" + templateName, entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("acknowledged") && json.getBoolean("acknowledged")) {
				return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 批量插入文档 index 带时间戳
	 * 
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	public static void bulkIndexDocument(String index, String type, Set<JSONObject> documents) throws Exception {
		if (documents.size() == 0) {
			return;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		index = index + "-" + dateFormat.format(new Date());
		StringBuilder jsonData = new StringBuilder();
		JSONObject action = new JSONObject();
		JSONObject meta_data = new JSONObject();
		meta_data.put("_index", index);
		meta_data.put("_type", type);
		action.put("index", meta_data);
		// 当数据量大于 500 时，进行分批提交
		int counter = 0;
		for (JSONObject json : documents) {
			jsonData.append(action.toString()).append("\n");
			jsonData.append(json.toString()).append("\n");
			counter += 1;
			if (documents.size() > 1000) {
				if (counter == 1000) {
					LOG.info("split bulkIndexDocument. Document size: {}", documents.size());
					bulkIndexDocument(jsonData.toString());
					counter = 0;
					jsonData = new StringBuilder();
				}
			}
		}
		if (counter > 0) {
			bulkIndexDocument(jsonData.toString());
		}
	}

	/**
	 * 批量插入文档 index 带时间戳
	 * 
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	public static void bulkIndexDocument1(String index, String type, Set<String> documents) throws Exception {
		if (documents.size() == 0) {
			return;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		index = index + "-" + dateFormat.format(new Date());
		StringBuilder jsonData = new StringBuilder();
		JSONObject action = new JSONObject();
		JSONObject meta_data = new JSONObject();
		meta_data.put("_index", index);
		meta_data.put("_type", type);
		// meta_data.put("_id", );
		action.put("index", meta_data);
		// 当数据量大于 500 时，进行分批提交
		int counter = 0;
		for (String doc : documents) {
			jsonData.append(action.toString()).append("\n");
			jsonData.append(doc).append("\n");
			counter += 1;
			if (documents.size() > 1000) {
				if (counter == 1000) {
					LOG.info("split bulkIndexDocument. Document size: {}", documents.size());
					bulkIndexDocument(jsonData.toString());
					counter = 0;
					jsonData = new StringBuilder();
				}
			}
		}
		if (counter > 0) {
			bulkIndexDocument(jsonData.toString());
		}
	}

	public static boolean postIndexDocument(String index, String type, String document) throws Exception {
		StringEntity entity = new StringEntity(document, ContentType.APPLICATION_JSON);
		String result = httpPost(getUrl() + "/" + index + "/" + type + "?refresh=true", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0) {
					return true;
				}
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 根据ID更新index
	 *
	 * @param index
	 * @param type
	 * @param id
	 * @param document
	 * @return
	 * @throws Exception
	 */
	public static boolean updateIndex(String index, String type, String id, String document) throws Exception {
		StringEntity entity = new StringEntity(document, ContentType.APPLICATION_JSON);
		String result = httpPut(getUrl() + "/" + index + "/" + type + "/" + id + "?refresh=true", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0) {
					return true;
				}
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 指定字段排序查询所有的document
	 *
	 * @param index
	 * @param type
	 * @param sortName
	 * @param isAsc
	 * @return
	 * @throws Exception
	 */
	public static String getAll(String index, String type, String sortName, boolean isAsc) throws Exception {
		String result = "";
		long size = count(index, type);
		try {
			result = httpGet(getUrl() + "/" + index + "/" + type + "/" + "_search?size=" + size + "&sort=" + sortName
					+ ":" + (isAsc ? "asc" : "desc"));
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0) {
					return json.toJSONString();
				}
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return result;
	}

	public static String getAll(String index, String type) throws Exception {
		String result = "";
		long size = count(index, type);
		try {
			result = httpGet(getUrl() + "/" + index + "/" + type + "/" + "_search?size=" + size);
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0) {
					return json.toJSONString();
				}
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return result;
	}

	/**
	 * 查看ES集群ES状态,只要不是green就返回false
	 * 
	 * @throws Exception
	 */
	public static boolean isGreen() throws Exception {
		boolean isGreen = false;
		String jsonStr = httpGet(getUrl() + "/_cluster/health");
		// String jsonStr = httpGet("http://172.16.42.18:8200" + "/_cluster/health");
		JSONObject json = JSONObject.parseObject(jsonStr);
		String status = json.getString("status");
		if ("green".equals(status)) {
			isGreen = true;
		}
		return isGreen;
	}

	public static long count(String index, String type) throws Exception {
		String result = httpGet(getUrl() + "/" + index + "/" + type + "/_count");
		long total = 0;
		JSONObject json = JSONObject.parseObject(result);
		if (json.containsKey("count")) {
			total = json.getLongValue("count");
		}
		return total;
	}

	/**
	 * 根据ID获取指定index数据
	 *
	 * @param index
	 * @param type
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static String getIndex(String index, String type, String id) throws Exception {
		String result = httpGet(getUrl() + "/" + index + "/" + type + "/" + id);
		return result;
	}

	/**
	 * 指定_id批量插入文档
	 *
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	public static void bulkIndexDocumentByID(String index, String type, Set<JSONObject> documents) throws Exception {
		StringBuilder jsonData = new StringBuilder();
		for (JSONObject source : documents) {
			String id = null;
			JSONObject action = new JSONObject();
			JSONObject meta_data = new JSONObject();
			meta_data.put("_index", index);
			meta_data.put("_type", type);
			if (source.containsKey("id")) {
				id = source.getString("id");
				source.remove("id");
				meta_data.put("_id", id);
				action.put("index", meta_data);

			}
			jsonData.append(action.toString()).append("\n");
			jsonData.append(source.toString()).append("\n");
		}
		bulkIndexDocument(jsonData.toString());
	}

	/**
	 * 批量插入文档
	 *
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	public static void bulkIndexDocument(String index, String type, String documents) throws Exception {
		StringBuilder jsonData = new StringBuilder();
		JSONObject action = new JSONObject();
		JSONObject meta_data = new JSONObject();
		meta_data.put("_index", index);
		meta_data.put("_type", type);
		action.put("index", meta_data);
		jsonData.append(action.toString()).append("\n");
		jsonData.append(documents).append("\n");
		StringEntity entity = new StringEntity(jsonData.toString(), ContentType.APPLICATION_JSON);
		String result = httpPost(getUrl() + "/_bulk", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("error") || json.getBoolean("errors")) {
				throw new RuntimeException("bulkIndexDocument failed! errors: " + json.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 批量插入文档
	 *
	 * @param documents
	 * @throws Exception
	 */
	public static void bulkIndexDocument(String documents) throws Exception {
		StringEntity entity = new StringEntity(documents, ContentType.APPLICATION_JSON);
		String result = httpPost(getUrl() + "/_bulk", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json != null && (json.containsKey("error") || json.getBoolean("errors"))) {
				throw new RuntimeException("bulkIndexDocument failed! errors: " + json.toString());
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * kyle
	 * 
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	public static void bulkIndexDocumentList(String index, String type, Set<String> document, int retryCount)
			throws Exception {
		List<String> documents = new ArrayList<>(document);
		int size = documents.size();
		if (0 == size) {
			return;
		}
		int batch = (int) Math.ceil((size / 1000.0));
		List<String> doc = null;
		int lastIndex = 0;
		// 数据大于1000条，分批次插入，每次1000条
		for (int i = 0; i < batch; i++) {
			lastIndex = (i + 1) * 1000;
			if (lastIndex > size) {
				lastIndex = size;
			}
			doc = documents.subList(i * 1000, lastIndex);
			bulkIndexDocumentSub(index, type, doc, retryCount);
		}
	}

	/**
	 * 
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	private static void bulkIndexDocumentSub(String index, String type, List<String> documents, int retryCount)
			throws Exception {
		if (0 == documents.size()) {
			return;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		index = index + "-" + dateFormat.format(new Date());
		StringBuilder jsonData = new StringBuilder();
		JSONObject action = new JSONObject();
		JSONObject meta_data = new JSONObject();
		meta_data.put("_index", index);
		meta_data.put("_type", type);
		action.put("index", meta_data);
		for (String doc : documents) {
			jsonData.append(action.toString()).append("\n");
			jsonData.append(doc).append("\n");
		}
		bulkIndexDocumentToES(jsonData.toString(), documents, retryCount);

	}

	/**
	 * kyle
	 * 
	 * @param documents
	 * @throws Exception
	 *             {"took":84,"errors":false,"items":[]}
	 */
	private static void bulkIndexDocumentToES(String doc, List<String> documents, int retryCount) throws Exception {
		StringEntity entity = new StringEntity(doc, ContentType.APPLICATION_JSON);
		String result = httpPost(getUrl() + "/_bulk", entity);
		JSONObject json = JSONObject.parseObject(result);
		List<Integer> errorIndex = null;
		List<String> retryDoc = null;
		// 判断是否有失败的记录
		if (null != json && json.getBoolean("errors")) {
			if (retryCount == 0) {
				LOG.warn("insert to es failed, data:" + doc);
				return;
			}
			errorIndex = getErrorIndexByResult(json);
			retryDoc = getRetryDoc(errorIndex, documents);
			bulkIndexDocumentToES(doc, retryDoc, --retryCount);
		}
	}

	// kyle
	private static List<Integer> getErrorIndexByResult(JSONObject json) {
		List<Integer> errorIndex = new ArrayList<>(500);
		String text = json.get("items").toString();
		JSONObject resultLineJson = null;
		List<JSONObject> items = JSONObject.parseArray(text, JSONObject.class);
		int i = 0;
		for (JSONObject item : items) {
			if (null != item) {
				resultLineJson = item.getJSONObject("index");
				if (null != resultLineJson && resultLineJson.containsKey("error")) {
					errorIndex.add(i);
				}
			}
			i++;
		}
		return errorIndex;
	}

	// kyle
	private static List<String> getRetryDoc(List<Integer> errorIndex, List<String> documents) {
		List<String> retryDoc = new ArrayList<String>(500);
		for (Integer index : errorIndex) {
			retryDoc.add(documents.get(index));
		}
		return retryDoc;
	}

	public static void main(String[] args) throws Exception {

		// init("http://10.16.238.82:9200");
		// String twitter = "{ \"template\" : \"te*\", \"settings\" : {
		// \"number_of_shards\" : 1 },
		// \"aliases\" : { \"alias1\" : {}, \"alias2\" : { \"filter\" : { \"term\" :
		// {\"user\" : \"kimchy\"
		// } }, \"routing\" : \"kimchy\" }, \"{index}-alias\" : {} } } ";
		// putTemplate("twitter", twitter);
		// System.out.println("template twitter:" + templateExists("twitter"));
		// System.out.println("template shoppingcartapi:" +
		// templateExists("shoppingcartapi"));
		//
		//
		//
		// String documents = "{\"index\":{}}\n{\"user\":\"value1\"}\n";
		// bulkIndexDocument("twitter", "tweet", documents);

	}

}
