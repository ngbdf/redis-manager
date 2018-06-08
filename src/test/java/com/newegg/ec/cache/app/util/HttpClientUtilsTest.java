package com.newegg.ec.cache.app.util;

import com.newegg.ec.cache.app.util.httpclient.HttpClientUtil;
import com.newegg.ec.cache.app.util.httpclient.RequestHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by lf52 on 2018/5/26.
 */
public class HttpClientUtilsTest {

    /**
     * test get 操作
     */
    @Test
    public void testGet(){
        String url = "http://localhost:8500/dockerapi/v2/containers";
        String containerId = "itemserviceSSL";
        try {
            String response = HttpClientUtil.getGetResponse(url, containerId);
            System.out.println(response);
            Assert.assertNotNull(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test 用户自定义操作，以get操作为例
     */
    @Test
    public void testReuqest(){

        try {
           String response =  HttpClientUtil.request(new RequestHandler<String>() {
                @Override
                public String callback(HttpClient client) throws Exception {

                    String url = "http://localhost:8500/dockerapi/v2/containers/itemserviceSSL";
                    String param = "";

                    if(StringUtils.isNotEmpty(param)){
                        url = url +"/"+ param;
                    }
                    HttpGet httpGet = new HttpGet(url);
                    httpGet.setHeader("Connection", "Keep-Alive");
                    httpGet.setHeader("Content-Type","application/json");

                    HttpResponse response;
                    try {
                        response = client.execute(httpGet);
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            return EntityUtils.toString(entity);
                        }

                    }finally {
                        httpGet.releaseConnection();
                    }
                    return null;
                }
            });
            System.out.println(response);
            Assert.assertNotNull(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
