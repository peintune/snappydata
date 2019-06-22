package com.tomiyo.snappy.snappyclient.util;

import org.apache.log4j.Logger;

import java.io.File;

public class Configparser
{
    private String filename=null;
    private String hostname="";
    private String proxyhost="localhost";
    private String proxyport="80";


    private  String proxyiphost="localhost";
    private boolean debugLog=true;
    private int contentparsernumber=5;
    private boolean autoproxyip=false;

    private static final Configparser configparser=new Configparser();
    Logger logger = Logger.getLogger(Configparser.class);

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
        
         contentparsernumber=Integer.parseInt(xmlFileParser.findElement("contentparsernumber").attributeValue("value"));
         hostname= xmlFileParser.findElement("hostname").attributeValue("value");
         debugLog=xmlFileParser.findElement("debuglog").attributeValue("value").equalsIgnoreCase("true")?true:false;
        autoproxyip=xmlFileParser.findElement("autoproxyip").attributeValue("value").equalsIgnoreCase("true")?true:false;
        proxyiphost=xmlFileParser.findElement("proxyiphost").attributeValue("value");
        try{
         proxyhost=xmlFileParser.findElement("proxy").element("proxyhost").attributeValue("value");
         proxyport=xmlFileParser.findElement("proxy").element("proxyport").attributeValue("value");
         }catch(Exception e){
             logger.info("no proxy setting, not use proxy");
         }
    }

    public int getContentparsernumber() {
        return contentparsernumber;
    }

    public boolean getAutoproxyip() {
        return autoproxyip;
    }
    public String getHostname()
    {
        return hostname;
    }
    public boolean getDebugLog()
    {
        return debugLog;
    }
    public String getProxyhost()
    {
        return proxyhost;
    }
    public String getProxyport()
    {
        return proxyport;
    }

    public String getProxyiphost() {
        return proxyiphost;
    }

    public void setProxyiphost(String proxyiphost) {
        this.proxyiphost = proxyiphost;
    }
}
