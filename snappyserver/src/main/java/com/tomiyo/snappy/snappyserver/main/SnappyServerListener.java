package com.tomiyo.snappy.snappyserver.main;


import com.tomiyo.snappy.snappyserver.contentParser.ContentParserManager;
import com.tomiyo.snappy.snappyserver.controller.Controller;
import com.tomiyo.snappy.snappyserver.envrionment.PrepareEnvironment;
import com.tomiyo.snappy.snappyserver.ipproxy.IpProxyManager;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.sender.MysqlSender;
import com.tomiyo.snappy.snappyserver.snappier.SnappierManager;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;



import java.util.Date;

public class SnappyServerListener implements ServletContextListener
{
    static Logger logger = Logger.getLogger(SnappyServerListener.class);

    private String[] _args = null;
    public static SnappierManager snappierManager=null;
    public static ContentParserManager contentParserManager=null;
    public static MysqlSender mysqlSender=null;
    public static Controller controller=null;

    public static void main(String[] args)
    {
        SnappyServerListener snappy=new SnappyServerListener(args);
        snappy.run();

    }
    public SnappyServerListener(){}
    public SnappyServerListener(String[] args){
        setArgs(args);
    }
    private void setArgs(String[] args)
    {
        this._args=args;
    }
    
    private void run()
    {
        logger.info("start snappier server");
        this.initialnize();
    }

    private void initialnize()
    {

      //  this.initProxy();
       // logger.info(" initialize http proxy ip manager success");

        this.initMysqlHandler();
        logger.info(" initialize http mysql handler  success");

        this.initHttpSenderManager();
        logger.info(" initialize http sender success");

         this.prepareEnvironment();
        logger.info(" prepare environment success");

//        this.initCmdManager();
//        logger.info(" initialize commands manager success");

        this.initSnappierManager();
        logger.info(" initialize snappier manager success");

//        this.initContentParserManager();
//        logger.info(" initialize content parser manager manager success");

        this.initSender();
        logger.info(" initialize results save manager  success");

        this.resumeAllSnappies();
        logger.info(" Start All snappies");


    }

    private void resumeAllSnappies() {

    }

    private void initProxy(){
       String proxyhost= Configparser.getInstance().getProxyhost();
       String proxyport= Configparser.getInstance().getProxyport();
       System.setProperty("http.proxyHost", proxyhost);  
       System.setProperty("http.proxyPort", proxyport);
        IpProxyManager ipProxyManager=new IpProxyManager();
      //  ipProxyManager.initialProxys();
    }

    private  void initContentParserManager(){
        //contentParserManager=new ContentParserManager();
    }


    private void initSender()
    {
        mysqlSender=new MysqlSender();
        Thread mysqlSenderThread=new Thread(mysqlSender,"Mysql Sender");
        mysqlSenderThread.setDaemon(true);
        mysqlSenderThread.start();
    }
    private void initMysqlHandler(){
        Configparser configparser=Configparser.getInstance();
        String hostname=configparser.getHostname();
        String portnumber=configparser.getPortnumber();
        String dbname=configparser.getDbname();
        String username=configparser.getUsername();
        String password=configparser.getPassword();
        MysqlHadler mysqlHadler=MysqlHadler.getInstance();
        mysqlHadler.setDbName(dbname);
        mysqlHadler.setHostName(hostname);
        mysqlHadler.setPassword(password);
        mysqlHadler.setPort(portnumber);
        mysqlHadler.setUsername(username);
    }

    private void initHttpSenderManager(){

    }

    private void initSnappierManager()
    {
         //snappierManager=new SnappierManager();
    }
    
    private void initController(){
        controller=Controller.getInstance();

        controller.registerSnappierManager(snappierManager);

        controller.registerContentParserManager(contentParserManager);

        new Thread(controller,"Controller").start();

       //controller.addOneContentParser();

       // controller.addSnappier("anjuke_esf","\\\\*");//休眠时间可以从配置文件读取
        // controller.startSnappier("anjuke_esf","\\\\*");

    }
    
    private void initCmdManager()
    {
        this.initController();
       // Executors.newScheduledThreadPool
    }

    private void prepareEnvironment(){
        PrepareEnvironment prepareEnvironment= new PrepareEnvironment();
        prepareEnvironment.initialEnvironment();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("start service");
       // SnappyServerListener snappy=new SnappyServerListener();
        this.run();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if(Thread.currentThread().isInterrupted())
        Thread.currentThread().interrupt();
    }
}
