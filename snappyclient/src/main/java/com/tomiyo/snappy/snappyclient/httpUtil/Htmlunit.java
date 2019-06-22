package com.tomiyo.snappy.snappyclient.httpUtil;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.client.params.CookiePolicy;

/**
 * Created by veronica1 on 16/7/24.
 */
public class Htmlunit {

    public static WebClient webClient = null;

    private static final  Htmlunit htmlUnit=new Htmlunit();

    public static Htmlunit getInstance(){

        if(null==htmlUnit){
            return new Htmlunit();
        }
        return  htmlUnit;
    }

    private Htmlunit(){
        String proxyHost=System.getProperty("proxyHost");
        if (proxyHost.length() > 0)
        {
            int port = Integer
                    .parseInt(System.getProperty("proxyPort"));
            webClient= new WebClient(BrowserVersion.CHROME,proxyHost,port);
        }else{
            webClient= new WebClient(BrowserVersion.CHROME);
        }
        System.setProperty("apache.commons.httpclient.cookiespec", CookiePolicy.BROWSER_COMPATIBILITY);
        webClient.getOptions().setRedirectEnabled(false);
        webClient.getCookieManager().setCookiesEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setTimeout(10*1000);
        webClient.waitForBackgroundJavaScript(6*1000);
        webClient.setJavaScriptTimeout(3*1000);
        webClient.getOptions().setUseInsecureSSL(false);
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
        // Webdrivier dr = new HtmlUnitDriver();

    }

    public  String doGet (String url)throws Exception{
        HtmlPage page=null;
        try {
            page = webClient.getPage(url);
            resetWebClientTimeout();
        }catch (Exception ell){
            increaseTimeout();
            return  doGet(url);
        }
        String pageXml = page.asXml();
        webClient.getCache().clear();
        return pageXml;

    }

    public WebClient getWebClient(){
        return webClient;
    }

    public void increaseTimeout(){
        webClient.waitForBackgroundJavaScript(webClient.getJavaScriptTimeout()+100);
        webClient.getOptions().setTimeout(webClient.getOptions().getTimeout()+1500);
        webClient.setJavaScriptTimeout(webClient.getJavaScriptTimeout()+100);
    }

    public void resetWebClientTimeout(){
        webClient.waitForBackgroundJavaScript(500);
        webClient.getOptions().setTimeout(1500);
        webClient.setJavaScriptTimeout(500);
    }
}
