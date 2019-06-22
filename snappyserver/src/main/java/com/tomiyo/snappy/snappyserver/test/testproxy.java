package com.tomiyo.snappy.snappyserver.test;

import com.tomiyo.snappy.snappyserver.httpUtil.HttpSender;

/**
 * Created by dell on 2017/5/24.
 */
public class testproxy {

    public static void main(String[] args){
    testproxy testproxy=new testproxy();
    String result=testproxy.getOneProxy();
    System.out.println(result);
    }

    public  String getOneProxy(){
        String url = "http://localhost:8080"+"/proxyip/rest/message/getoneproxyip";
        String result="";
        try {
            result= HttpSender.doGet4HttpClient(url,false).trim();
        } catch (Exception e) {
            e.printStackTrace();
          //  logger.error(e.getMessage());
        }

        return result;
    }
}
