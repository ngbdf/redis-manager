package com.newegg.ec.redis.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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
 * @author：Truman.P.Du
 * @createDate: 2018年10月16日 下午2:40:35
 * @version:1.0
 * @description:汇报者
 */
public class Report {
	private static final Logger LOG = LoggerFactory.getLogger(Report.class);

	
	private static PoolingHttpClientConnectionManager cm;
	// 设置超时时间
	private static int REQUEST_TIMEOUT;
	private static int REQUEST_SOCKET_TIME;
	private static String EMPTY_STR = "";
	private static String serverUrl;

	static {
		cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
	}

	public static void init(Integer requestTimeout, Integer socketTimeout,String url) {
		REQUEST_TIMEOUT = requestTimeout;
		REQUEST_SOCKET_TIME = socketTimeout;
		String tempUrl = url.substring(0, url.length()-1);
		serverUrl = tempUrl.substring(0, tempUrl.lastIndexOf("/"));
	}

	private static CloseableHttpClient getHttpClient() {

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT)
				.setSocketTimeout(REQUEST_SOCKET_TIME).build();

		return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
	}

	public static void reportStatus(String status) {
		try {
			LOG.info("Starting report status...");
			StringEntity entity = new StringEntity(status, ContentType.APPLICATION_JSON);
			httpPost(serverUrl+"/receiveStatus", entity);
			LOG.info("Done report status...");
		} catch (Exception e) {
			LOG.info("Report status ERROR: ", e);
		}
	}
	
	public static void report2Server(String message) {
		try {
			LOG.info("Starting report to server...");
			StringEntity entity = new StringEntity(message, ContentType.APPLICATION_JSON);
			httpPost(serverUrl+"/receiveAnalyzeResult", entity);
			LOG.info("Done report to server...");
		} catch (Exception e) {
			LOG.info("Report  to server ERROR: ", e);
		}
	}

	/**
	 * 处理Http请求
	 *
	 * @param request
	 * @return
	 */
	protected static String getResult(HttpRequestBase request) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf-8");
				response.close();
				return result;
			}
		} catch (Exception e) {
			LOG.error("Report message has error.", e);
		}

		return EMPTY_STR;
	}

	protected static String httpHead(String url) {
		HttpHead httpHead = new HttpHead(url);
		return getResult(httpHead);
	}

	protected static String httpPost(String url, HttpEntity entity) {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		return getResult(httpPost);
	}
	
	public static void main(String[] args) {
		Report.init(30000, 30000, "http://localhost:8080/eureka/");
		JSONObject message = new JSONObject();
		message.put("key", "hello");
		Report.report2Server(message.toJSONString());
	}

}
