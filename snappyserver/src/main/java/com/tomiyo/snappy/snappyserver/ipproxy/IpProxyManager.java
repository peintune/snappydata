package com.tomiyo.snappy.snappyserver.ipproxy;

import java.util.*;

/**
 * Created by I322353 on 9/11/2016.
 */
public class IpProxyManager {

    //public static  List<ProxyEntity> proxyList=new ArrayList<>();
    public static HashMap<String,ProxyEntity> proxyList=new HashMap<>();

    public void initialProxys(){
        XiCiProxy xiCiProxy=new XiCiProxy();
        Thread xiCiThread = new Thread(xiCiProxy,"Proxy parser");
        xiCiThread.setDaemon(true);
        xiCiThread.start();

//        while(true) {
//            if (!xiCiThread.isAlive()) {
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }else{
//                break;
//            }
//        }
//            while (true) {
//                int length = proxyList.size();
//                if (length == 0) {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    break;
//                }
//            }
    }


//    public synchronized  static String getOneProxy(){
//        return ":";
//    }
//
    public synchronized  static String getOneProxy(boolean useful){
        return ":";
    }
    public synchronized  static String getOneProxy(){

//        if(proxyList.size()<1){
//            XiCiProxy xiCiProxy=new XiCiProxy();
//            xiCiProxy.startOnce();
//        }
        //System.out.println("当前proxy数量为"+proxyList.size());

        //int random2=(int)(Math.random()*0);
        //if(random2==0)return "60.13.46.27:8080";
       // if(random2==0 || random2==589)return "183.245.147.15:8080";
         int length= proxyList.size();

        int random=(int)(Math.random()*(length-1));
        //return "222.87.75.169:80";
//        try {
//            String[] keys = proxyList.keySet().toArray(new String[0]);
//            String randomKey = keys[random];
//            ProxyEntity randomEntity = proxyList.get(randomKey);
//            return randomEntity.getHost() + ":" + randomEntity.getPort();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        return ":";
    }

//    public synchronized  static String getOneProxy(boolean useful){
//
//        if(useful){
//            if(proxyList.size()<1){
//                XiCiProxy xiCiProxy=new XiCiProxy();
//                xiCiProxy.startOnce();
//            }
//
//            int length=proxyList.size();
//            if(length<1)return ":";
//            int random=(int)(Math.random()*(length-1));
//
//            for(int i=0;i<length;i++) {
//                String[] keys = proxyList.keySet().toArray(new String[0]);
//                String randomKey = keys[random];
//                ProxyEntity randomEntity = proxyList.get(randomKey);
//                if(randomEntity.getLike()>randomEntity.getUnlike()) {
//                    return randomEntity.getHost() + ":" + randomEntity.getPort();
//                }else{
//                    continue;
//                }
//            }
//        }else{
//            return getOneProxy();
//        }
//        return ":";
//    }

    public static void like(String proxyString){
        if(null==proxyString||null==proxyList)return;
        if(!proxyList.containsKey(proxyString))return;
        synchronized (proxyList) {
            proxyList.get(proxyString).addLike();
            proxyList.get(proxyString).addLike();
        }
    }


    public static void unlike(String proxyString) {
        if(null==proxyString||null==proxyList)return;
        if(!proxyList.containsKey(proxyString))return;
        synchronized (proxyList) {
            proxyList.get(proxyString).addUnlike();

            if (proxyList.get(proxyString).getUnlike() < 10) {
                return;
            }
            if (proxyList.get(proxyString).getLike() +5< proxyList.get(proxyString).getUnlike()) {
                proxyList.remove(proxyString);
            }
        }
    }

    public  static void addProxys(List<ProxyEntity> tempProxy){

            for(ProxyEntity entity:tempProxy){
                String key=entity.getHost()+":"+entity.getPort();
                if(!proxyList.containsKey(key))
                proxyList.put(key,entity);
            }
            return;

    }
}
