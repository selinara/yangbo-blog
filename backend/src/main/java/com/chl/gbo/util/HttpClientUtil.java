package com.chl.gbo.util;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * HttpClient4.5.3 访问工具类
 * @author minsun
 *
 */
public class HttpClientUtil {
    private static final Log logger = LogFactory.getLog(HttpClientUtil.class);

    public static final String ERROR_HTTP_RESPONSE_CODE = "500";
    public static final String ERROR_HTTP_TIMEOUT_RESPONSE_CODE = "504";

    private static final String APPLICATION_JSON = "application/json";

    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";

    private static PoolingHttpClientConnectionManager manager = null;
    private static CloseableHttpClient httpClient = null;

    public static synchronized CloseableHttpClient getHttpClient() {
        if(httpClient == null) {
            //注册访问协议相关的工厂
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
            //HttpConnectionFactory工厂
            HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory =
                    new ManagedHttpClientConnectionFactory(DefaultHttpRequestWriterFactory.INSTANCE,
                            DefaultHttpResponseParserFactory.INSTANCE);
            //DNS解析器
            DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
            //创建连接池
            manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connFactory, dnsResolver);
            //默认Socket配置
            SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            manager.setDefaultSocketConfig(defaultSocketConfig);
            //整个连接池大小
            manager.setMaxTotal(300);
            //每个路由的最大连接池
            manager.setDefaultMaxPerRoute(200);
            //从连接池中获取连接时，连接不活跃的多长时间需要进行一次验证
            manager.setValidateAfterInactivity(5 * 1000);

            //请求配置
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setConnectTimeout(5500)
                    .setSocketTimeout(5500)
                    .setConnectionRequestTimeout(2 * 1000).build();

            //创建HttpClient
            httpClient = HttpClients.custom().setConnectionManager(manager)
                    .setConnectionManagerShared(false)//是否共享连接池
                    .evictIdleConnections(60, TimeUnit.SECONDS)//定期回收空闲连接
                    .evictExpiredConnections()//定期回收过期连接
                    .setConnectionTimeToLive(60, TimeUnit.SECONDS)//连接存活时间
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
                    .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)//设置是否keepAlive
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();//设置重试次数

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        httpClient.close();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
        }

        return httpClient;
    }

    /**
     * HttpClient4.5.3 GET访问方式
     * @param url
     * @param paramsMap
     * @param charset
     * @return
     * @throws Exception
     */
    public static String doGet(String url, Map<String, String> paramsMap, String charset) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            if(paramsMap == null || paramsMap.isEmpty()){
                return ERROR_HTTP_RESPONSE_CODE;
            }
            HttpGet httpGet = new HttpGet(url);
            List<NameValuePair> pairs = new ArrayList<NameValuePair>(paramsMap.size());
            for(Map.Entry<String,String> entry : paramsMap.entrySet()){
                String value = entry.getValue();
                if(value != null){
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + paramsStr));
            response = httpClient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                logger.info("responseCode=" + responseCode + ", url=" + url + ", params=" + JSONObject.toJSONString(paramsMap));
                return ERROR_HTTP_RESPONSE_CODE;
            }
            return EntityUtils.toString(response.getEntity());
        } catch (SocketTimeoutException ex) {
            logger.error("http get occurs timeout error, url=" + url + ", params=" + JSONObject.toJSONString(paramsMap), ex);
            return ERROR_HTTP_TIMEOUT_RESPONSE_CODE;
        } catch (Exception e) {
            logger.error("http get occurs exception error, url=" + url + ", params=" + JSONObject.toJSONString(paramsMap), e);
            return ERROR_HTTP_RESPONSE_CODE;
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    /**
     * HttpClient4.5.3 POST访问方式
     * @param url
     * @param paramsMap
     * @param charset
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, String> paramsMap, String charset) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            if(paramsMap == null || paramsMap.isEmpty()){
                return ERROR_HTTP_RESPONSE_CODE;
            }
            List<NameValuePair> pairs = new ArrayList<NameValuePair>(paramsMap.size());
            for(Map.Entry<String,String> entry : paramsMap.entrySet()){
                String value = entry.getValue();
                if(value != null){
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pairs, charset);
            httpPost.setEntity(urlEncodedFormEntity);
            response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                logger.info("responseCode=" + responseCode + ", url=" + url + ", params=" + JSONObject.toJSONString(paramsMap));
                return ERROR_HTTP_RESPONSE_CODE;
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            return result;
        } catch (SocketTimeoutException ex) {
            logger.error("http post occurs timeout error, url=" + url + ", params=" + JSONObject.toJSONString(paramsMap), ex);
            return ERROR_HTTP_TIMEOUT_RESPONSE_CODE;
        } catch (Exception e) {
            logger.error("http post occurs exception error, url=" + url + ", params=" + JSONObject.toJSONString(paramsMap), e);
            return ERROR_HTTP_RESPONSE_CODE;
        }finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }


    /**
     * HttpClient4.5.3 POST访问方式
     * @param url
     * @param json
     * @param charset
     * @return
     * @throws Exception
     */
    public static String doPost(String url, String json, String charset) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            if(StringUtils.isEmpty(json)){
                return ERROR_HTTP_RESPONSE_CODE;
            }
//            String encoderJson = URLEncoder.encode(json, charset);
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);

            StringEntity se = new StringEntity(json);
            se.setContentType(CONTENT_TYPE_TEXT_JSON);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                logger.info("responseCode=" + responseCode + ", url=" + url + ", params=" + json);
                return ERROR_HTTP_RESPONSE_CODE;
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            return result;
        } catch (SocketTimeoutException ex) {
            logger.error("http post occurs timeout error, url=" + url + ", params=" + json, ex);
            return ERROR_HTTP_TIMEOUT_RESPONSE_CODE;
        } catch (Exception e) {
            logger.error("http post occurs exception error, url=" + url + ", params=" + json, e);
            return ERROR_HTTP_RESPONSE_CODE;
        }finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

}