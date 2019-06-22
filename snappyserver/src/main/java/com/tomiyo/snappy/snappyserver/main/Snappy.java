package com.tomiyo.snappy.snappyserver.main;


import java.util.Date;

import com.tomiyo.snappy.snappyserver.contentParser.ContentParserManager;
import com.tomiyo.snappy.snappyserver.controller.Controller;
import com.tomiyo.snappy.snappyserver.envrionment.PrepareEnvironment;
import com.tomiyo.snappy.snappyserver.ipproxy.IpProxyManager;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.sender.MysqlSender;
import com.tomiyo.snappy.snappyserver.snappier.SnappierManager;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import org.apache.log4j.Logger;

public class Snappy 
{
    static Logger logger = Logger.getLogger(Snappy.class);

    private String[] _args = null;
    public static SnappierManager snappierManager=null;
    public static ContentParserManager contentParserManager=null;
    public static MysqlSender mysqlSender=null;
    public static  Controller controller=null;
    public static void main(String[] args)
    {
        Snappy snappy=new Snappy(args);
        snappy.run();

    }

    public Snappy(String[] args){
        setArgs(args);
    }
    private void setArgs(String[] args)
    {
        this._args=args;
    }
    
    private void run()
    {
        System.out.println(new Date()+ "  开始启动程序……");
        this.initialnize();//初始化整个程序
    }

    private void initialnize()
    {

        this.initProxy();//初始化http代理,此处会block住后面的操作，如果没有代理，则主线程会block
        logger.info(" initialize http proxy ip manager success");

        this.initMysqlHandler();//初始化操作数据库管理者
        logger.info(" initialize http mysql handler  success");

        this.initHttpSenderManager();
        logger.info(" initialize http sender success");

        this.prepareEnvironment();//初始化环境
        logger.info(" prepare environment success");

       this.initCmdManager();//初始化命令管理者
        logger.info(" initialize commands manager success");

        this.initSnappierManager();//初始化爬虫管理者
        logger.info(" initialize snappier manager success");

//        this.initContentParserManager();
//        logger.info(" initialize content parser manager manager success");

        this.initSender();//初始化发送者，发送爬取的内容到存储介质，目前是mysql
        logger.info(" initialize results save manager  success");


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
        mysqlSender=new MysqlSender();//初始化mysql发送者
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
         //snappierManager=new SnappierManager();//初始化爬虫管理者
        //实际上不做任何事情，因为爬虫管理者是全局唯一共享的。
    }
    
    private void initController(){
        //controller=new Controller();
        controller = Controller.getInstance();
        controller.registerSnappierManager(snappierManager);//往控制器中注册爬虫管理者

        controller.registerContentParserManager(contentParserManager);//往控制器中注册内容爬取管理者

        new Thread(controller,"Controller").start();

//        controller.addOneContentParser();
        controller.startSnappier("anjuke_esf","anjuke_esf_shanghai");

       // controller.addSnappier("anjuke_esf","\\\\*");//休眠时间可以从配置文件读取
         //controller.startSnappier("anjuke_esf","\\\\*");
        //controller.startSnappier("anjuke_esf","anjuke_esf_baotou");
        // .addSnappier("anjuke","\\\\*");//休眠时间可以从配置文件读取
        //controller.addSnappier("anjuke","anjuke_xf_baotou");//休眠时间可以从配置文件读取

       // controller.startSnappier("anjuke_esf","anjuke_esf_beijing");//休眠时间可以从配置文件读取
//
       // controller.addSnappier("anjuke_esf","anjuke_esf_shanghai");//休眠时间可以从配置文件读取
        //controller.initialLoadSnappier("anjuke_ershoufang","anjuke_esf_shanghai");

       // controller.addSnappier("anjuke_esf","anjuke_esf_akesu");//休眠时间可以从配置文件读取
        //controller.initialLoadSnappier("anjuke_ershoufang","anjuke_esf_shanghai");

       // controller.addSnappier("anjuke_esf","anjuke_esf_al");//休眠时间可以从配置文件读取
        //controller.initialLoadSnappier("anjuke_ershoufang","anjuke_esf_shanghai");
//        controller.startSnappier("anjuke_esf","anjuke_esf_al");
//        controller.startSnappier("anjuke_esf","anjuke_esf_chongqing");
//        controller.startSnappier("anjuke_esf","anjuke_esf_cs");
//        controller.startSnappier("anjuke_esf","anjuke_esf_cz");
//        controller.startSnappier("anjuke_esf","anjuke_esf_beijing");
//        controller.startSnappier("anjuke_esf","anjuke_esf_beijing");
//        controller.startSnappier("anjuke_esf","anjuke_esf_shanghai");
//        controller.startSnappier("anjuke_esf","anjuke_esf_akesu");


//        //controller.initialLoadSnappier("anjuke_ershoufang","anjuke_esf_shanghai");
        //        controller.addSnappier("anjuke_esf","\\\\*");//休眠时间可以从配置文件读取

//

        //controller.addSnappier("anjuke","anjuke_xf_bd");//休眠时间可以从配置文件读取
        //controller.initialLoadSnappier("anjuke","anjuke_xf_bd");
        //controller.startSnappier("anjuke","anjuke_xf_bd");
        //controller.addSnappier("anjuke_ershoufang","anjuke_esf_shanghai3");//休眠时间可以从配置文件读取
        //controller.initialLoadSnappier("anjuke_ershoufang","anjuke_esf_shanghai3");
        //controller.startSnappier("anjuke_ershoufang","anjuke_esf_shanghai3");
        //controller.addSnappier("anjuke","anjuke_xf_shanghai");
       // controller.addSnappier("anjuke",1);
    }
    
    private void initCmdManager()
    {
        this.initController();//初始化爬虫控制器
       // Executors.newScheduledThreadPool
    }

    private void prepareEnvironment(){
        PrepareEnvironment prepareEnvironment= new PrepareEnvironment();
        prepareEnvironment.initialEnvironment();
    }
}
