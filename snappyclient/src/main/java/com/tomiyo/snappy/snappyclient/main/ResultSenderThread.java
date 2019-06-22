package com.tomiyo.snappy.snappyclient.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomiyo.snappy.snappyclient.httpUtil.HttpSender;
import com.tomiyo.snappy.snappyclient.message.ContentMessage;
import com.tomiyo.snappy.snappyclient.message.IMessage;
import com.tomiyo.snappy.snappyclient.message.MessageManager;
import org.apache.log4j.Logger;

/**
 * Created by I322353 on 12/27/2016.
 */
public class ResultSenderThread implements Runnable{
    private static String testUrl;
    private  static boolean needProxy=true;
    public static Logger logger = Logger.getLogger(ResultSenderThread.class);
    static ObjectMapper mapper = new ObjectMapper();
    @Override
    public void run() {

        initial();

        while(true){
            if(MessageManager.hasMessage()){
                IMessage contentMessage = MessageManager.fetchOneMessage();
                if(contentMessage!=null){
                    sendMessage(contentMessage);
                }
            }else{
                try {
                    Thread.currentThread().sleep(30*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sendAllMessage(){
        initial();
        while(MessageManager.hasMessage()){
                IMessage contentMessage = MessageManager.fetchOneMessage();
                if(contentMessage!=null){
                    sendMessage(contentMessage);
                }
            }
    }
    public static void initial(){
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        testUrl=System.getProperty("hostName")+"/message/postOneMessage";
        if(System.getProperty("hostName").trim().contains("localhost")||System.getProperty("hostName").trim().contains("127.0.0.1")){
            needProxy=false;
        }
    }

    public  static void sendMessage(IMessage message){
        String jsonMessage=null;
        try {
             jsonMessage =  mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.error("failed to generate json string from content message");
            logger.error(e.getCause());
        }
        if(null!=jsonMessage){
            try {
                String result= HttpSender.doPostJsonRequest(testUrl,jsonMessage,"",needProxy);
                if(result.contains("success")){
                    logger.info("push one content message to server success");
                }else{
                    logger.error("failed to send content message to "+testUrl);
                }
            } catch (Exception e) {
                logger.error("failed to send content message to "+testUrl);
                logger.error(e.getCause());

            }
        }

    }

    public  static void main(String[] args){
        testUrl="http://localhost:8080/message/postOneMessage";
        ResultSenderThread rs=new ResultSenderThread();
        ContentMessage message= new ContentMessage();
        message.setUrlLinkAddress("test1ssssss");
        rs.sendMessage(message);

    }
}
