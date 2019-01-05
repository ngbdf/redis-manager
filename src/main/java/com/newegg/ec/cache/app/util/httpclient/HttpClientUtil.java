package com.newegg.ec.cache.app.util.httpclient;


import com.newegg.ec.cache.core.logger.CommonLogger;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
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
 * Created by lf52 on 2017/9/29.
 */
public class HttpClientUtil {

    private static final CommonLogger logger = new CommonLogger(HttpClientUtil.class);
    private static HttpClient httpclient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(10 * 60 * 1000).build());
        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(50);
        httpclient = HttpClientBuilder.create()
                // set keep_alive strategy
                .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

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
                .setConnectionManager(cm).setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)).build();

        //start expiredConnections thread
        IdleConnectionMonitorThread idleConnectionMonitorThread = new IdleConnectionMonitorThread(cm);
        idleConnectionMonitorThread.start();
    }

    /**
     * @param url
     * @param json_post
     * @return
     * @throws IOException
     */
    public static String getPostResponse(String url, JSONObject json_post) throws IOException {
        HttpPost httpPost = postForm(url, json_post);
        HttpResponse response;
        String result = null;

        try {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } finally {
            httpPost.releaseConnection();
        }

        return result;
    }

    /**
     * @param url
     * @param jsonParam
     * @return
     */
    private static HttpPost postForm(String url, JSONObject jsonParam) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Connection", "Keep-Alive");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        try {
            StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");//解决中文乱码问题
            httpPost.setEntity(entity);

        } catch (Exception e) {
            logger.error("create HttpPost error:", e);
        }
        return httpPost;
    }

    public static String getGetResponse(String url, String param) throws IOException {
        HttpGet httpGet = getForm(url, param);
        HttpResponse response;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }

        } finally {
            httpGet.releaseConnection();
        }

        return null;
    }

    /*
     * @param url
     * @return
     */
    private static HttpGet getForm(String url, String param) {

        if (StringUtils.isNotEmpty(param)) {
            url = url + "/" + param;
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Connection", "Keep-Alive");
        httpGet.setHeader("Content-Type", "application/json");
        return httpGet;
    }

    public static String getPutResponse(String url, JSONObject json_put) throws IOException {
        HttpPut httpPut = putForm(url, json_put);
        HttpResponse response;
        String result = null;
        try {
            response = httpclient.execute(httpPut);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }

        } finally {
            httpPut.releaseConnection();
        }

        return result;
    }

    /*
     * @param url
     * @return
     */
    private static HttpPut putForm(String url, JSONObject jsonParam) {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Connection", "Keep-Alive");
        httpPut.setHeader("Content-Type", "application/json");
        try {
            StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");//解决中文乱码问题
            httpPut.setEntity(entity);

        } catch (Exception e) {
            logger.error("create httpPut error:", e);
        }
        return httpPut;
    }

    public static String getDeleteResponse(String url, String param) throws IOException {
        HttpDelete httpDelete = deleteForm(url, param);
        HttpResponse response;
        String result = null;
        try {
            response = httpclient.execute(httpDelete);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }

        } finally {
            httpDelete.releaseConnection();
        }

        return result;
    }

    private static HttpDelete deleteForm(String url, String param) {
        if (StringUtils.isNotEmpty(param)) {
            url = url + "/" + param;
        }
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Connection", "Keep-Alive");
        httpDelete.setHeader("Content-Type", "application/json");
        return httpDelete;
    }

    /**
     * 通过回调的方式用户自己扩展HttpClientUtil中未实现的API：如OPTIONS TRACE HEAD等不常用的操作
     *
     * @param requestHandler
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T request(RequestHandler<T> requestHandler) throws Exception {
        return requestHandler.callback(httpclient);
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
                        //close expired && free up 30s connections
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
