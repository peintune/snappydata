package com.tomiyo.snappy.snappyclient.main;

import com.tomiyo.snappy.snappyclient.util.Configparser;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by hekun on 12/27/2016.
 */
public class SnappyClientServeletListener implements ServletContextListener {
    String hostName = "http://localhost:8080";
    int contentParserNumber = 5;
    static Logger logger = Logger.getLogger(SnappyClientServeletListener.class);
    Thread thread3= null;
    Thread thread2= null;
    public SnappyClientServeletListener(){}

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
          //  IpProxyManager ipProxyManager = new IpProxyManager();
            //ipProxyManager.initialProxys();
            logger.info("Start auto proxy ip success");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Start Snappyier Client");
        SnappyClientServeletListener client = new SnappyClientServeletListener();
        client.initial();

        logger.info("initialize  Snappyier Client  success");


         thread3 = new Thread(new ResultSenderThread(), "ResultSenderThread");
       // thread3.setDaemon(true);
        thread3.start();
        logger.info("Start Result Sender  Thread success");

        thread2= new Thread(new ContentParserThread(), "ContentParserThread");
        thread2.start();
        logger.info("Start Content Parser  Thread success");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (thread2 != null && thread2.isInterrupted()) {
            thread2.interrupt();
        }
        if (thread3 != null && thread3.isInterrupted()) {
            thread3.interrupt();
        }
    }
}



