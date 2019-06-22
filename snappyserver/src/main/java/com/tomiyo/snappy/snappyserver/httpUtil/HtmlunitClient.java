package com.tomiyo.snappy.snappyserver.httpUtil;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import com.tomiyo.snappy.snappyserver.ipproxy.IpProxyManager;
import org.apache.http.client.params.CookiePolicy;
import org.apache.log4j.Logger;

/**
 * Created by hekun on 16/7/24.
 */
public class HtmlunitClient{

    private  WebClient webClient = null;

    private  int tryTimes=0;

    public String url="";

    public String host="";

    public int port=8080;

    boolean jsEnable=false;

    boolean noProxy=false;

    Logger logger = Logger.getLogger(HtmlunitClient.class);

    public  HtmlunitClient(boolean jsEnable,String proxyHost,int proxyPort,boolean noProxy){
        init(jsEnable,proxyHost,proxyPort,noProxy);
        //reset();
    }

    public HtmlunitClient(boolean jsEnable) {
            init(jsEnable,null,0,false);
    }

    private  void init(boolean jsEnable,String proxyHost,int proxyPort,boolean noProxy){

        this.jsEnable=jsEnable;
        this.host=proxyHost;
        this.port=proxyPort;
        this.noProxy=noProxy;
        //  Boolean autoProxyIp=Boolean.parseBoolean(System.getProperty("isAutoProxyIP"));
        Boolean autoProxyIp=true;

        if(noProxy){
            webClient= new WebClient(BrowserVersion.CHROME);
        }else{
            if(!autoProxyIp){
                String proxyHostFromConfig=System.getProperty("proxyHost");
                int proxyPortFromConfig=Integer.parseInt(System.getProperty("proxyPort"));

                if(proxyHostFromConfig.length()>0 &&proxyPortFromConfig>0){
                    webClient= new WebClient(BrowserVersion.CHROME,proxyHostFromConfig,proxyPortFromConfig);
                    ProxyConfig proxyConfig = new ProxyConfig();
                    proxyConfig.setProxyHost(proxyHostFromConfig);
                    proxyConfig.setProxyPort(proxyPortFromConfig);
                    webClient.getOptions().setProxyConfig(proxyConfig);
                }else{
                    webClient= new WebClient(BrowserVersion.CHROME);
                }
            }else{
                if(null!=proxyHost&& null!=new Integer(proxyPort)) {
                    // webClient = new WebClient(BrowserVersion.CHROME, proxyHost, proxyPort);
                    webClient = new WebClient(BrowserVersion.CHROME);
                    ProxyConfig proxyConfig = new ProxyConfig();
                    proxyConfig.setProxyHost(proxyHost);
                    proxyConfig.setProxyPort(proxyPort);
                    webClient.getOptions().setProxyConfig(proxyConfig);
                }else{
                    webClient= new WebClient(BrowserVersion.CHROME);

                }
            }
        }

        System.setProperty("apache.commons.httpclient.cookiespec", CookiePolicy.BROWSER_COMPATIBILITY);
        //webClient.getOptions().setRedirectEnabled(false);
        // webClient.getOptions().setRedirectEnabled(true);
        webClient.getCookieManager().setCookiesEnabled(false);
        if(jsEnable) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.waitForBackgroundJavaScript(5*1000);//6*1000
            webClient.setJavaScriptTimeout(3*1000);//3*1000
        }else{
            webClient.getOptions().setJavaScriptEnabled(false);

        }
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.setHTMLParserListener(null);
        webClient.setJavaScriptErrorListener(null);
        webClient.setRefreshHandler(new ThreadedRefreshHandler());
        webClient.getOptions().setTimeout(8*1000);//10*1000
        webClient.getOptions().setUseInsecureSSL(true);
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
    }



    public  String doGet (String url)throws Exception{
        Page page=null;
        try {
            page = webClient.getPage(url);
           // resetWebClientTimeout();
        }catch (Exception ell){
            tryTimes++;
            if(tryTimes>1){
                logger.error("request for  "+url+" error at "+ tryTimes+" times");
                logger.error(ell.getCause());
                return "";
            }
            clear();
            increaseTimeout();
            return  doGet(url);
        }
        if(null==page){
            tryTimes++;
            clear();
            increaseTimeout();
            if(tryTimes>1)return "";
            return   doGet(url);
        }
        String pageXml="";

        if(page.isHtmlPage()){
            pageXml =((HtmlPage)page).asXml();
        }else{
            logger.warn(((TextPage)page).getContent()+"");
            if(tryTimes>1)return "";
        }
        this.close();
        return pageXml;
    }

    public String doGetOneTime(String url) throws  Exception{
        String result="";
        HtmlPage page=null;
        try {
            page = webClient.getPage(url);
        }catch (Exception ell){
         return "";
        }
        if(null!=page){
            result=page.asXml();
        }
        return  result;
    }
    public WebClient getWebClient(){
        return webClient;
    }

    public void increaseTimeout(){
        webClient.waitForBackgroundJavaScript(webClient.getJavaScriptTimeout()+500);
        webClient.getOptions().setTimeout(webClient.getOptions().getTimeout()+1000);
        webClient.setJavaScriptTimeout(webClient.getJavaScriptTimeout()+500);
    }

    public void resetWebClientTimeout(){
        webClient.waitForBackgroundJavaScript(6*1000);
        webClient.getOptions().setTimeout(10*1000);
        webClient.setJavaScriptTimeout(3*1000);
    }

    public void clear(){
        try {
            webClient.getWebConnection().close();
            webClient.getJavaScriptEngine().shutdown();
            webClient.getCache().clear();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void invalidProxy(){
        if(!noProxy){
            IpProxyManager.unlike(host+":"+port);
        }
    }
    public void validProxy(){
        if(!noProxy){
            IpProxyManager.like(host+":"+port);
        }
    }
    public void reinit(){
        webClient=null;
        init(jsEnable,null,port,noProxy);
        reset();
    }

    public void reinit(boolean jsEnable,String proxyHost,int proxyPort,boolean noProxy){
        webClient=null;
        init(jsEnable,proxyHost,proxyPort,noProxy);
        reset();
    }
    public void reset(){
       this.resetWebClientTimeout();
        try {
            webClient.getWebConnection().close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }finally {
            webClient.getJavaScriptEngine().shutdown();
            webClient.getCache().clear();
            tryTimes=0;
        }

    }

    public void close(){
        try {
            webClient.getWebConnection().close();
            webClient.getJavaScriptEngine().shutdown();
            webClient.getCache().clear();
            webClient.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    public void setURL(String url){
        this.url=url;
    }

    public  String getOneProxy(){
        String url = "http://localhost:8080"+"/proxyip/rest/message/getoneproxyip";
        String result="";
        try {
            result= HttpSender.doGet4HttpClient(url,false).trim();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }finally {

        }
        return result;
    }
}
