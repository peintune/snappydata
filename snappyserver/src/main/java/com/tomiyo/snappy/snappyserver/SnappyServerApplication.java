/**
 *
 */
package com.tomiyo.snappy.snappyserver;

import com.tomiyo.snappy.snappyserver.contentParser.ContentParserManager;
import com.tomiyo.snappy.snappyserver.controller.Controller;
import com.tomiyo.snappy.snappyserver.envrionment.PrepareEnvironment;
import com.tomiyo.snappy.snappyserver.ipproxy.IpProxyManager;
import com.tomiyo.snappy.snappyserver.main.SnappyServerListener;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.sender.MysqlSender;
import com.tomiyo.snappy.snappyserver.snappier.SnappierManager;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @description: 主入口类
 * @author: He Kun
 * @create: 2018-05-17 14:18
 **/
@SpringBootApplication
public class SnappyServerApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {

    run();
    ConfigurableApplicationContext run = SpringApplication.run(SnappyServerApplication.class, args);
  }




  static Logger logger = Logger.getLogger(SnappyServerListener.class);

  private String[] _args = null;
  public static SnappierManager snappierManager=null;
  public static ContentParserManager contentParserManager=null;
  public static MysqlSender mysqlSender=null;
  public static Controller controller=null;


  private void setArgs(String[] args)
  {
    this._args=args;
  }

  private static void run()
  {
    logger.info("start snappier server");
    initialnize();
  }

  private static void initialnize()
  {

    //  this.initProxy();
    // logger.info(" initialize http proxy ip manager success");

    initMysqlHandler();
    logger.info(" initialize http mysql handler  success");

    initHttpSenderManager();
    logger.info(" initialize http sender success");

    prepareEnvironment();
    logger.info(" prepare environment success");

//        this.initCmdManager();
//        logger.info(" initialize commands manager success");

    initSnappierManager();
    logger.info(" initialize snappier manager success");

//        this.initContentParserManager();
//        logger.info(" initialize content parser manager manager success");

    initSender();
    logger.info(" initialize results save manager  success");

    resumeAllSnappies();
    logger.info(" Start All snappies");


  }

  private static void resumeAllSnappies() {

  }

  private static void initProxy(){
    String proxyhost= Configparser.getInstance().getProxyhost();
    String proxyport= Configparser.getInstance().getProxyport();
    System.setProperty("http.proxyHost", proxyhost);
    System.setProperty("http.proxyPort", proxyport);
    IpProxyManager ipProxyManager=new IpProxyManager();
    //  ipProxyManager.initialProxys();
  }

  private  static void initContentParserManager(){
    //contentParserManager=new ContentParserManager();
  }


  private static void initSender()
  {
    mysqlSender=new MysqlSender();
    Thread mysqlSenderThread=new Thread(mysqlSender,"Mysql Sender");
    mysqlSenderThread.setDaemon(true);
    mysqlSenderThread.start();
  }
  private static void initMysqlHandler(){
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

  private static void initHttpSenderManager(){

  }

  private static void initSnappierManager()
  {
    //snappierManager=new SnappierManager();
  }

  private static void initController(){
    controller=Controller.getInstance();

    controller.registerSnappierManager(snappierManager);

    controller.registerContentParserManager(contentParserManager);

    new Thread(controller,"Controller").start();

    //controller.addOneContentParser();

    // controller.addSnappier("anjuke_esf","\\\\*");//休眠时间可以从配置文件读取
    // controller.startSnappier("anjuke_esf","\\\\*");

  }

  private static void initCmdManager()
  {
    initController();
    // Executors.newScheduledThreadPool
  }

  private static void prepareEnvironment(){
    PrepareEnvironment prepareEnvironment= new PrepareEnvironment();
    prepareEnvironment.initialEnvironment();
  }
}
