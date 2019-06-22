package com.tomiyo.snappy.snappyclient.main;

import com.tomiyo.snappy.snappyclient.util.Configparser;
import org.apache.log4j.Logger;

/**
 * Created by I322353 on 12/27/2016.
 */
class SnappyClient {
    String hostName = "http://localhost:8080";
    int contentParserNumber = 5;
    static Logger logger = Logger.getLogger(SnappyClient.class);

    public static void main(String[] args) {
        logger.info("Start Snappyier Client");
        SnappyClient client = new SnappyClient();
        client.initial();

        logger.info("initialize  Snappyier Client  success");


        Thread thread3 = new Thread(new ResultSenderThread(), "ResultSenderThread");
        //thread3.setDaemon(true);
        thread3.start();
        logger.info("Start Result Sender  Thread success");

        Thread thread2= new Thread(new ContentParserThread(), "ContentParserThread");
        thread2.start();
        logger.info("Start Content Parser  Thread success");



    }

    public void initial() {
        hostName = Configparser.getInstance().getHostname();
        contentParserNumber = Configparser.getInstance().getContentparsernumber();

        boolean isFree = true;

        if(isFree){
            if(contentParserNumber >5)contentParserNumber =5;
        }

        System.setProperty("hostName",hostName);
        System.setProperty("proxyHost",Configparser.getInstance().getProxyhost());
        System.setProperty("proxyPort",Configparser.getInstance().getProxyport());
        System.setProperty("isAutoProxyIP",String.valueOf(Configparser.getInstance().getAutoproxyip()));
        System.setProperty("failedTimes",String.valueOf(0));
        System.setProperty("contentParserNumber",String.valueOf(contentParserNumber));

        if(Boolean.parseBoolean(System.getProperty("isAutoProxyIP"))) {
           // IpProxyManager ipProxyManager = new IpProxyManager();
            //ipProxyManager.initialProxys();
            //logger.info("Start auto proxy ip success");
        }
    }

}



