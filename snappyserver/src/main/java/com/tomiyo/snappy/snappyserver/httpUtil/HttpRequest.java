package com.tomiyo.snappy.snappyserver.httpUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HttpRequest
{
    
    public static String sendGet2(String url) throws Exception{
        String result = "";
        BufferedReader in = null;
        String urlNameString = url ;
        URL realUrl = new URL(urlNameString);
        URLConnection connection = realUrl.openConnection();
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("Content-type", "text/html");
        connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        //connection.setRequestProperty("host", "comic.qq.com");
        connection.connect();
        in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"GBK"));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return result;
         //return  new String(buffer,"gbk");
    }
    public static  String sendGet3(String urlString)throws Exception{
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Content-type", "text/html");
        //conn.setRequestProperty("contentType", "UTF-8");
        conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
       // conn.setRequestProperty("Accept-Charset", "UTF-8"); 
        conn.connect();
        BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line="";
        StringBuffer buffer=new StringBuffer();
        while((line=reader.readLine())!=null){
         buffer.append(line);
        }
        String result="";
        result=buffer.toString();
        result=new String(buffer.toString().getBytes("iso-8859-1"),"utf-8");
        return result;
    }
}
