package com.tomiyo.snappy.snappyclient.main;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomiyo.snappy.snappyclient.threadpool.ContentParserManager;
import com.tomiyo.snappy.snappyclient.httpUtil.HttpSender;
import com.tomiyo.snappy.snappyclient.message.MessageManager;
import com.tomiyo.snappy.snappyclient.message.URLMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by I322353 on 12/27/2016.
 */
public class ContentParserThread implements Runnable{
    Logger logger = Logger.getLogger(ContentParserThread.class);
    ObjectMapper mapper = new ObjectMapper();
    String hostName=System.getProperty("hostName");
    private boolean needProxy=true;
    int contentParserNumber=Integer.parseInt(System.getProperty("contentParserNumber"));
    @Override
    public void run() {
            initial();
            boolean isIdle = false;
            boolean isLostConnection = false;
            while (true) {

                if(MessageManager.getQueueNumber()>30){
                    ResultSenderThread.sendAllMessage();
                }

                boolean isConnectAble = checkConnectionToServer();
                if (isConnectAble) {
                    if (isLostConnection) {
                        logger.info("re-connection to " + hostName + " success");
                    }
                    isLostConnection = false;
                } else {
                    if (!isLostConnection) {
                        logger.error("can't connection to " + hostName + "  , try to reconnect latter");
                    }
                    sleepSeconds(60);
                    continue;
                }

                int activeContentParser =  ContentParserManager.getActiveCtentParserCount();
                if (contentParserNumber ==  activeContentParser) {
                    sleepSeconds(5);
                    continue;
                }

                for (int i = 0; i < contentParserNumber - activeContentParser; i++) {
                    URLMessage urlMessage = getOneMessage();
                    if (urlMessage != null) {
                        if (isIdle) {
                            isIdle = false;
                        }

                        ContentParserManager.addOneContentParser(urlMessage);
                        logger.info("Process one message");
                    } else {
                        isIdle = true;
                    }
                }

                int failedTimes = Integer.parseInt(System.getProperty("failedTimes"));

                if (failedTimes >= contentParserNumber) {
                    logger.warn("network issue,idle this client for " + contentParserNumber * 5 + " seconds");
                    //sleepSeconds(contentParserNumber * 5);
                    System.setProperty("failedTimes",String.valueOf(0));
                }

                if (isIdle) {
                    logger.info("no message comes,idle this client for 300 seconds");
                    sleepSeconds(300);
                    ContentParserManager.resetCountOfContentParser();
                }
            }
    }

    public void initial(){
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if(System.getProperty("hostName").trim().contains("localhost")||System.getProperty("hostName").trim().contains("127.0.0.1")){
            needProxy=false;
        }
    }
    public URLMessage getOneMessage() {

        String testApi = hostName + "/message/fetchOneMessage";
        URLMessage urlMessage = null;
        try {
            String result = HttpSender.doGet4HttpClient(testApi,needProxy);
            urlMessage = mapper.readValue( JSONObject.parseObject(result).get("data").toString(), URLMessage.class);

        } catch (Exception e) {
           // logger.error("can't reach " + testApi+" error is :"+e.getMessage());
        }
        return urlMessage;
    }

    public boolean checkConnectionToServer(){
        String hostTmp=hostName.split("http://")[1];
        String host="";
        int hostPort=0;
        if(hostTmp.contains(":")){
            host=hostTmp.split(":")[0];
             hostPort=Integer.parseInt(hostTmp.split(":")[1]);
        }else{
            host=hostTmp;
        }

        boolean isConnectAbale=false;
        if(host.length()>3&&hostPort>0){
            isConnectAbale =  isHostConnectable(host,hostPort);
        }else if(host.length()>0){
            isConnectAbale =  isHostConnectable(host,0);
        }else{
            logger.error("server host name and port not a valid value");
        }
        return  isConnectAbale;
    }
    public void sleepSeconds(int seconds){
        try {
            Thread.currentThread().sleep(seconds*1000);
            //TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            logger.error(e.getCause());
        }
    }

    public  boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
           if(port>0) {
               socket.connect(new InetSocketAddress(host, port), 1500);
           }else{
               InetAddress[] addresses = InetAddress.getAllByName(host);
               if(addresses.length>0){
                   return true;
               }else{
                   return false;
               }
           }
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error(e.getCause());
            }
        }
        return true;
    }

}
