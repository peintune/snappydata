package com.tomiyo.snappy.snappyserver.util;

import com.tomiyo.snappy.snappyserver.httpUtil.HttpSender;

import static com.tomiyo.snappy.snappyserver.util.ContentParserThreadPool.logger;

/**
 * Created by dell on 2017/7/23.
 */
public class HttpProxyIp {

    public static String getOneBestProxy(String groupname){
        String url="";
        if(null!=groupname&&groupname.trim()!=""){
            url = Configparser.getInstance().getProxyiphost()+"/proxyip/rest/message/getonebestproxyip/"+groupname;
        }else{
            url = Configparser.getInstance().getProxyiphost()+"/proxyip/rest/message/getonebestproxyip/null";
        }
        String result="";
        try {
            result= HttpSender.doGet4HttpClient(url,false).trim();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }finally {

        }
        return result;
    }

}
