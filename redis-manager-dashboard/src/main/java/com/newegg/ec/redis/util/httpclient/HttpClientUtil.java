package com.newegg.ec.redis.util.httpclient;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author lf52
 * @date 2017/9/29
 */
public class HttpClientUtil {

    private static final String CONNECTION = "Connection";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String KEEP_ALIVE = "Keep-Alive";

    private static final String ACCEPT = "Accept";

    private static final String APPLICATION_JSON = "application/json";

    private static final String UTF8 = "utf-8";

    private static CloseableHttpClient httpclient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(10 * 60 * 1000).build());
        // 最大连接数
        cm.setMaxTotal(10);
        // 最大并发数
        cm.setDefaultMaxPerRoute(10);
        httpclient = HttpClientBuilder.create()
                // set keep_alive strategy
                .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
                    /**
                     * 实例化连接池，设置连接池管理器。
                     * 这里需要以参数形式注入上面实例化的连接池管理器
                     *
                     * @return
                     */
                    @Override
                    public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                        HeaderElementIterator it = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
                        while (it.hasNext()) {
                            HeaderElement he = it.nextElement();
                            String param = he.getName();
                            String value = he.getValue();
                            if (value != null && param.equalsIgnoreCase("timeout")) {
                                try {
                                    return Long.parseLong(value) * 1000;
                                } catch (NumberFormatException ignore) {
                                }
                            }
                        }
                        return 5 * 1000;
                    }
                })
                .setConnectionManager(cm).setRetryHandler(new DefaultHttpRequestRetryHandler(2, true)).build();
        IdleConnectionMonitorThread idleConnectionMonitorThread = new IdleConnectionMonitorThread(cm);
        idleConnectionMonitorThread.start();
    }


    public static String post(String url, JSONObject postJson) throws IOException {
        HttpPost httpPost = postForm(url, postJson);
        HttpResponse response;
        String result = null;
        try {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, UTF8);
            }
        } finally {
            httpPost.releaseConnection();
        }
        return result;
    }

    /**
     * @param url
     * @param param
     * @return
     */
    private static HttpPost postForm(String url, JSONObject param) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(getRequestConfig());
        httpPost.setHeader(CONNECTION, KEEP_ALIVE);
        httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        httpPost.setHeader(ACCEPT, APPLICATION_JSON);
        StringEntity entity = new StringEntity(param.toString(), UTF8);
        httpPost.setEntity(entity);
        return httpPost;
    }

    public static String get(String url) throws IOException {
        HttpGet httpGet = getForm(url);
        HttpResponse response;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, UTF8);
        } finally {
            httpGet.releaseConnection();
        }
    }

    /**
     * @param url
     * @return
     */
    private static HttpGet getForm(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(getRequestConfig());
        httpGet.addHeader(ACCEPT, APPLICATION_JSON);
        httpGet.addHeader(CONNECTION, KEEP_ALIVE);
        httpGet.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        return httpGet;
    }

    /**
     * 设置代理及其他配置
     *
     * @return
     */
    public static RequestConfig getRequestConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setConnectionRequestTimeout(3000)
                .build();
        return requestConfig;
    }


    static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;

        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super("Idle Connection Monitor Thread");
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            while (!shutdown) {
                synchronized (this) {
                    try {
                        wait(1000 * 5);
                        // close expired && free up 30s connections
                        connMgr.closeExpiredConnections();
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    } catch (Throwable ignore) {
                    }
                }
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }


}
