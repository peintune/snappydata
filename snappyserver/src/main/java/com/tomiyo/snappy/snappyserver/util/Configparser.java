package com.tomiyo.snappy.snappyserver.util;

import java.io.File;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class Configparser
{
    Logger logger = Logger.getLogger(Configparser.class);

    private String filename=null;
    private Document doc=null;
    private String hostname="";
    private String portnumber="";
    private String dbname="";
    private String username="";
    private String password="";
    //这两个暂时不用
    private String proxyhost="";
    private String proxyport="80";

    public String getProxyiphost() {
        return proxyiphost;
    }

    public void setProxyiphost(String proxyiphost) {
        this.proxyiphost = proxyiphost;
    }

    //获取http ip的host地址
    private  String proxyiphost="localhost";

    private boolean debugLog=true;
    private int parallelexecutornumber=5;
    private static final Configparser configparser=new Configparser();
    private Configparser(){
        String file=PathManager.getRootPath()+File.separator+"config.xml";
        this.filename=file;
        this.initialnize();
    }
    
    public static Configparser getInstance(){
        return configparser;
    }


    
    private void initialnize(){
        
         XmlFileParser xmlFileParser=new XmlFileParser(filename);
        
         parallelexecutornumber=Integer.parseInt(xmlFileParser.findElement("snappier").element("parallelexecutornumber").attributeValue("value"));
         hostname= xmlFileParser.findElement("mysql").element("hostname").attributeValue("value");
         portnumber= xmlFileParser.findElement("mysql").element("portnumber").attributeValue("value");
         dbname= xmlFileParser.findElement("mysql").element("dbname").attributeValue("value");
         username= xmlFileParser.findElement("mysql").element("username").attributeValue("value");
         password= xmlFileParser.findElement("mysql").element("password").attributeValue("value");
         debugLog=xmlFileParser.findElement("debuglog").attributeValue("value").equalsIgnoreCase("true")?true:false;
         try{
         proxyhost=xmlFileParser.findElement("proxy").element("proxyhost").attributeValue("value");
         proxyport=xmlFileParser.findElement("proxy").element("proxyport").attributeValue("value");                     proxyiphost=xmlFileParser.findElement("proxyiphost").attributeValue("value");
         }catch(Exception e){
             logger.info("no proxy setting");
         }
    }
    
    public int getParallelexecutornumber()
    {
        return parallelexecutornumber;
    }

    public String getHostname()
    {
        return hostname;
    }

    public String getPortnumber()
    {
        return portnumber;
    }

    public String getDbname()
    {
        return dbname;
    }

    public String getUsername()
    {
        return username;
    }
    public boolean getDebugLog()
    {
        return debugLog;
    }
    public String getPassword()
    {
        return password;
    }
    public String getProxyhost()
    {
        return proxyhost;
    }

    public String getProxyport()
    {
        return proxyport;
    }
}
