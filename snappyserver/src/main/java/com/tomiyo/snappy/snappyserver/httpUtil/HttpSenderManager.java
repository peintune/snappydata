package com.tomiyo.snappy.snappyserver.httpUtil;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i322353 on 8/21/2016.
 */
public class HttpSenderManager {
    private  static GenericKeyedObjectPoolConfig conf;
    static {
         conf = new GenericKeyedObjectPoolConfig();
        conf.setMaxTotal(500);
        conf.setMaxIdlePerKey(50);
        conf.setMaxTotalPerKey(100);
        conf.setMinIdlePerKey(5);
    }
    public static final HttpSenderPoolFactory httpSenderPoolFactory=new HttpSenderPoolFactory();
    public static final GenericKeyedObjectPool<Boolean,HtmlunitClient> pool = new GenericKeyedObjectPool<Boolean, HtmlunitClient>(httpSenderPoolFactory, conf);



    public static HtmlunitClient getHtmlUnitClient(boolean isEnableJS) {
        HtmlunitClient client= null;
        try {
            client = (HtmlunitClient) pool.borrowObject(isEnableJS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    public static List<HtmlunitClient> getHtmlUnitClients(int number, boolean isEnableJS) {
        List<HtmlunitClient> lists=new ArrayList<HtmlunitClient>();
        for(int i=0;i<number;i++){

            try {
                lists.add((HtmlunitClient) pool.borrowObject(isEnableJS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lists;
    }


    public static void returnHtmlUnitClient(boolean isEnableJS,HtmlunitClient htmlunitClient){
        try {
            htmlunitClient.reset();
            pool.returnObject(isEnableJS,htmlunitClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void returnHtmlUnitClient(boolean isEnableJS,List<HtmlunitClient> htmlunitClients){
        for(HtmlunitClient htmlunitClient:htmlunitClients){
            try {
                htmlunitClient.reset();
                pool.returnObject(isEnableJS,htmlunitClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getAvailableClientCounts(boolean isEnableJS){
          return  pool.getNumActive(isEnableJS);
    }

}
