package com.tomiyo.snappy.snappyclient.httpUtil;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class HttpSender
{
    static String currentCookieString = "";

    static Logger logger = Logger.getLogger(HttpSender.class);

    public static String doGet(String testApi) throws  Exception{

        if(false){
           return doGet4HttpClient(testApi,true);
        }else{
            try {
                return Htmlunit.getInstance().doGet(testApi);
            }catch (Exception e){
                logger.error(e.getCause());
            }
            return "";
        }

    }



    public static String doGet4HttpClient(String testApi,boolean needProxy) throws Exception
    {

        String requestUrl = testApi;
        BufferedReader in = null;
        String result = "";
        String cookieStrings = currentCookieString;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = doGet(httpclient,requestUrl, cookieStrings,needProxy);
        String cookies = getCookie(response);
        currentCookieString = cookies;
        Header[] headers;
        String referUrl = "";
        List<String> cookieList = new ArrayList<String>();
        boolean requestOk = true;
        if (response.getStatusLine().toString().contains("Moved Temporarily"))
        {
            requestOk = false;
        }
        while (!requestOk)
        {
            if (response.getStatusLine().toString()
                    .contains("Moved Temporarily"))
            {
                headers = response.getAllHeaders();
                for (Header location : headers)
                {
                    if (location.getName().equalsIgnoreCase("Set-Cookie"))
                    {
                        cookieList.add(location.getValue());
                    }
                    if (location.getName().equalsIgnoreCase("location"))
                    {
                        referUrl = location.getValue();
                        for (String cookie : cookieList)
                        {
                            currentCookieString += cookie.split(";")[0] + ";";


                        }
                        if (currentCookieString.endsWith(";"))
                        {
                            currentCookieString = currentCookieString.substring(
                                    0, currentCookieString.length() - 1);
                        }

                        response = doGet(httpclient,referUrl, currentCookieString);
                    }
                }
            }
            else
            {
                requestOk = true;
            }
        }

        in = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuffer sb = new StringBuffer("");
        String line = "";
        String NL = System.getProperty("line.separator");
        while ((line = in.readLine()) != null)
        {
            sb.append(line + NL);
        }
        in.close();
        result = sb.toString();
        httpclient.close();
        return result;
    }

    /**
     * @return the String the method of send by Get type
     * @throws Exception
     */
    private static HttpResponse doGet(CloseableHttpClient httpClient,String urlString, String cookieStrings) {
         return   doGet(httpClient,urlString,cookieStrings,true);
    }

    /**
     * @return the String the method of send by Get type
     * @throws Exception
     */
    private static HttpResponse doGet(CloseableHttpClient httpClient,String urlString, String cookieStrings,boolean needProxy)
    {

        HttpResponse response = null;
        HttpGet request = new HttpGet("/");
        String proxyHostFromConfig=System.getProperty("proxyHost");
        int proxyPortFromConfig=Integer.parseInt(System.getProperty("proxyPort"));
        if (needProxy&&proxyHostFromConfig.length() > 0)
        {

            HttpHost proxy = new HttpHost(proxyHostFromConfig, proxyPortFromConfig, "http");
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(9000)
                .setConnectTimeout(9000)
                .setConnectionRequestTimeout(9000)
                .setStaleConnectionCheckEnabled(true)
                .setProxy(proxy)
                    .build();
            request.setConfig(config);
        }
        String cookieString = "";

        if (currentCookieString.length() > 2)
        {
            request.addHeader("Cookie", currentCookieString);
        }
        else if (null != cookieString && cookieString.length() > 3)
        {
            request.addHeader("Cookie", cookieString);
        }
        currentCookieString = cookieStrings;

        try
        {
            request.setURI(new URI(urlString));

            response = httpClient.execute(request);
        }
        catch (Exception e)
        {
                for(int i=0;i<3;i++){
                    try {
                        response = httpClient.execute(request);
                        if(null!=response)break;
                    }catch (Exception e2){
                    }
                }
        }
        finally
        {

        }
        if (response.getStatusLine ().getStatusCode () != 200) {
            request.abort();
        }
        return response;
    }

    private static HttpClient getHttpClient()
    {
        String proxyHostFromConfig=System.getProperty("proxyHost");
        int proxyPortFromConfig=Integer.parseInt(System.getProperty("proxyPort"));
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet request = new HttpGet("/");
        if (proxyHostFromConfig.length() > 0)
        {

            HttpHost proxy = new HttpHost(proxyHostFromConfig, proxyPortFromConfig, "http");
            RequestConfig config = RequestConfig.custom().setProxy(proxy)
                    .build();
            request.setConfig(config);
        }
        return httpclient;
    }

    public static String doPost(String testApi, String parameters)
            throws Exception
    {
        String strResult = "";

        String requestUrl = testApi;
        String cookieStrings = "";
        cookieStrings = currentCookieString;
        List<String> cookieList = new ArrayList<String>();
        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response = doPostRequest(httpclient,requestUrl, cookieStrings);
        String cookies = getCookie(response);
        currentCookieString = cookies;

        String referUrl = "";
        Header[] headers;
        boolean requestOk = true;
        if (response.getStatusLine().toString().contains("Moved Temporarily"))
        {
            requestOk = false;
        }
        while (!requestOk)
        {
            if (response.getStatusLine().toString()
                    .contains("Moved Temporarily"))
            {
                headers = response.getAllHeaders();
                for (Header location : headers)
                {
                    if (location.getName().equalsIgnoreCase("Set-Cookie"))
                    {
                        cookieList.add(location.getValue());
                    }
                    if (location.getName().equalsIgnoreCase("location"))
                    {
                        referUrl = location.getValue();
                        for (String cookie : cookieList)
                        {
                            // if(cookie.startsWith("guid=")||cookie.startsWith("per_")||cookie.startsWith("cityid")){
                            currentCookieString += cookie.split(";")[0] + ";";
                            // }

                        }
                        if (currentCookieString.endsWith(";"))
                        {
                            currentCookieString = currentCookieString.substring(
                                    0, currentCookieString.length() - 1);
                        }
                        response = doPostRequest(httpclient,referUrl, currentCookieString);
                    }
                }
            }
            else
            {
                requestOk = true;
            }
        }
        strResult = EntityUtils.toString(response.getEntity());
        return strResult;
    }

    /**
     * @return the String the main method of send by Post type
     * @throws Exception
     */
    public static HttpResponse doPostRequest(HttpClient httpclient,String url, String cookieStrings)
            throws Exception
    {
        String requestUrl = url;
        String[] urlArrayStrings = requestUrl.split("\\?");

        HttpPost httppost = new HttpPost(urlArrayStrings[0]);
        String proxyHostFromConfig=System.getProperty("proxyHost");
        int proxyPortFromConfig=Integer.parseInt(System.getProperty("proxyPort"));
        if (proxyHostFromConfig.length() > 0)
        {

            HttpHost proxy = new HttpHost(proxyHostFromConfig, proxyPortFromConfig, "http");
            RequestConfig config = RequestConfig.custom().setProxy(proxy)
                    .build();
            httppost.setConfig(config);
        }
        String cookieString = "";
        httppost.addHeader("charset", HTTP.UTF_8);
        if (currentCookieString.length() > 2)
        {
            httppost.addHeader("Cookie", currentCookieString);
        }
        else if (null != cookieString && cookieString.length() > 3)
        {
            httppost.addHeader("Cookie", cookieString);
        }
        currentCookieString = cookieStrings;
        if (urlArrayStrings.length > 1)
        {
            String[] parmsArry = urlArrayStrings[1].split("&");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                    parmsArry.length);
            for (int i = 0; i < parmsArry.length; i++)
            {
                String valueString = "";
                if (parmsArry[i].split("=").length > 1)
                {
                    valueString = parmsArry[i].split("=")[1];
                }

                nameValuePairs
                        .add(new BasicNameValuePair(parmsArry[i].split("=")[0],
                                valueString.replace("\"", "")));
            }
            httppost.setEntity(
                    new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
        }

        HttpResponse response;
        response = httpclient.execute(httppost);
        if (response.getStatusLine ().getStatusCode () != 200) {
            httppost.abort();
            // return null;
        }
        return response;
    }

    /**
     * @return the String the main method of send by Post type
     * @throws Exception
     */
    public static String doPostJsonRequest(String url, String json,String cookieStrings,boolean needProxy)
            throws Exception
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        String proxyHostFromConfig=System.getProperty("proxyHost");
        int proxyPortFromConfig=Integer.parseInt(System.getProperty("proxyPort"));
        if (needProxy&&proxyHostFromConfig.length() > 0)
        {

            HttpHost proxy = new HttpHost(proxyHostFromConfig, proxyPortFromConfig, "http");
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setStaleConnectionCheckEnabled(true)
                    .setProxy(proxy)
                    .build();
            httppost.setConfig(config);
        }
        String cookieString = "";

        httppost.addHeader("charset", HTTP.UTF_8);
        httppost.addHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(json,"utf-8");//解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httppost.setEntity(entity);

        if (currentCookieString.length() > 2)
        {
            httppost.addHeader("Cookie", currentCookieString);
        }
        else if (null != cookieString && cookieString.length() > 3)
        {
            httppost.addHeader("Cookie", cookieString);
        }
        currentCookieString = cookieStrings;

        HttpResponse response;
        response = httpclient.execute(httppost);

        if (response.getStatusLine ().getStatusCode () != 200) {
            httppost.abort();
            // return null;
        }
        String resData="fail";

        resData = EntityUtils.toString(response.getEntity());
        response =null;
        httpclient.close();
        return resData;
    }


    /**
     * @return get the cookie
     */
    private static String getCookie(HttpResponse response)
    {
        Header[] headers;
        String cookieString = "";
        List<String> cookieList = new ArrayList<String>();
        headers = response.getAllHeaders();
        for (Header location : headers)
        {
            if (location.getName().equalsIgnoreCase("Set-Cookie"))
            {
                cookieList.add(location.getValue());
            }
            if (location.getName().equalsIgnoreCase("location"))
            {
                for (String cookie : cookieList)
                {
                    cookieString += cookie.split(";")[0] + ";";

                }
                if (cookieString.endsWith(";"))
                {
                    cookieString = cookieString.substring(0,
                            cookieString.length() - 1);
                }
            }

        }
        return cookieString;
    }

}
