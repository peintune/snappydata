package com.tomiyo.snappy.snappyclient.httpUtil;

import java.util.concurrent.Callable;

/**
 * Created by I322353 on 9/16/2016.
 */
public class HttpSenderThread  implements Callable<String> {

    private HtmlunitClient htmlunitClient=null;
    private String url="";

    public  HttpSenderThread(HtmlunitClient htmlunitClient,String url){
            this.htmlunitClient=htmlunitClient;
            this.url=url;
    }
    @Override
    public String call() throws Exception {
        String resultHtml="";
        try {
                resultHtml=this.htmlunitClient.doGet(url);
                return resultHtml;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultHtml;

    }
}
