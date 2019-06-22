package com.tomiyo.snappy.snappyclient.ipproxy;

import com.tomiyo.snappy.snappyclient.httpUtil.HtmlunitClient;
import com.tomiyo.snappy.snappyclient.xsoup.Xsoup;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by I322353 on 9/11/2016.
 */
public class XiCiProxy implements  Runnable{

    String url="http://www.xicidaili.com/nn/";
    String url1="http://www.xicidaili.com/wn/";
    //String url2="http://www.kuaidaili.com/free/inha/";
   // String url3="http://www.kuaidaili.com/free/outha/";
   // String url4="http://www.66ip.cn/nmtq.php?getnum=40&isp=0&anonymoustype=3&start=&ports=8080&export=&ipaddress=&area=1&proxytype=2&api=66ip";
    String url4="http://www.66ip.cn/nmtq.php?getnum=20&isp=0&anonymoustype=4&start=&ports=8080&export=&ipaddress=&area=1&proxytype=2&api=66ip";
    Logger logger = Logger.getLogger(XiCiProxy.class);

    @Override
    public void run() {

        boolean isFisrt=true;
        String proxyHost=System.getProperty("proxyHost");
        int proxyPort=Integer.parseInt(System.getProperty("proxyPort"));
    while (true) {
//if(isFisrt){

        HtmlunitClient httpClient = null;
        if (proxyHost.length() > 0) {
            httpClient = new HtmlunitClient(false, proxyHost, proxyPort, false);
        } else {
            httpClient = new HtmlunitClient(false, null, 0, true);
        }
        if(!isFisrt){
             httpClient = new HtmlunitClient(false);
        }
        isFisrt=false;
        int random=(int)(Math.random()*5)+1;
        switch (random){
            case 1:
            case 2:
                parseProxys(httpClient,url);
                break;
            case 3:
            case 4:
            case 5:
                parseProxys2(httpClient,url4);
                break;
            default:
                break;
        }

        try {
            httpClient.reset();
            Thread.currentThread().sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    }

    public void startOnce(){
        HtmlunitClient httpClient = null;
        if (System.getProperty("proxyHost").length() > 0) {
            String host = System.getProperty("proxyHost");
            int port = Integer
                    .parseInt(System.getProperty("proxyPort"));
            httpClient = new HtmlunitClient(false, host, port, false);
        } else {
            httpClient = new HtmlunitClient(false, null, 0, true);
        }
        int random=(int)(Math.random()*5)+1;
        random=5;
        switch (random){
            case 1:
            case 2:
                parseProxys(httpClient,url);
                break;
            case 3:
            case 4:
            case 5:
                HtmlunitClient htmlunitClient=new HtmlunitClient(true,null,0,true);
                parseProxys2(htmlunitClient,url4);
                break;
            default:
                break;
        }

    }


    public void parseProxys2(HtmlunitClient httpClient,String oneurl){


        String resultHtml="";
        try {
            resultHtml=  httpClient.doGet(oneurl);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(resultHtml.length()<100)return;

        Document doc = Jsoup.parse(resultHtml);

        Elements hostResults=null;

        switch (oneurl){
            case "http://www.66ip.cn/nmtq.php?getnum=40&isp=0&anonymoustype=3&start=&ports=8080&export=&ipaddress=&area=1&proxytype=2&api=66ip":
                hostResults = Xsoup.compile("/html/body/text()").evaluate(doc).getElements();
                break;
            case "http://www.66ip.cn/nmtq.php?getnum=20&isp=0&anonymoustype=4&start=&ports=8080&export=&ipaddress=&area=1&proxytype=2&api=66ip":
                hostResults = Xsoup.compile("/html/body/text()").evaluate(doc).getElements();
                break;
            default:
                break;

        }

        if(null== hostResults||hostResults.size()<1)return;

        List<ProxyEntity> proxyListTemp=new ArrayList<>();

        String[] proxys=hostResults.get(0).text().split(" ");
        for(int i=0;i<proxys.length;i++){
            String host="";
            int port=0;
            if(proxys[i].trim().length()>10)

                try {
                     host = proxys[i].split(":")[0];

                     port = Integer.parseInt(proxys[i].split(":")[1]);
                }catch (Exception e){
                    continue;
                }

            boolean is8080=is8080(port);
            if(is8080) {
                if(isHostConnectable(host,port)) {
                    ProxyEntity entity = new ProxyEntity();
                    entity.setHost(host);
                    entity.setPort(port);
                    proxyListTemp.add(entity);
                }
            }else{
                continue;
            }
        }

        if(proxyListTemp.size()>0) {
            IpProxyManager.addProxys(proxyListTemp);
           logger.info("get " + proxyListTemp.size() + " proxy Ip sucess");
        }
    }

    public void parseProxys(HtmlunitClient httpClient,String oneurl){

        String resultHtml="";
        try {
          resultHtml=  httpClient.doGet(oneurl);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(resultHtml.length()<200)return;

        Document doc = Jsoup.parse(resultHtml);

        Elements hostResults=null;
        Elements portResults=null;

        switch (oneurl){
            case "http://www.xicidaili.com/nn/":
            case "http://www.xicidaili.com/wn/":
                hostResults = Xsoup.compile("//*[@id=\"ip_list\"]/tbody/tr[@class=\"odd\"]/td[2]").evaluate(doc).getElements();
                portResults = Xsoup.compile("//*[@id=\"ip_list\"]/tbody/tr[@class=\"odd\"]/td[3]").evaluate(doc).getElements();
                break;
            case "http://www.kuaidaili.com/free/inha/":
            case "http://www.kuaidaili.com/free/outha/":
                hostResults = Xsoup.compile("//*[@id=\"list\"]/table/tbody/tr/td[1]").evaluate(doc).getElements();
                portResults = Xsoup.compile("//*[@id=\"list\"]/table/tbody/tr/td[2]").evaluate(doc).getElements();
                break;
            default:
                break;

        }

        if(hostResults.size()!=portResults.size())return;

        List<ProxyEntity> proxyListTemp=new ArrayList<>();

        for(int i=0;i<hostResults.size();i++){
            String host=  hostResults.get(i).text();
              int port =Integer.parseInt(portResults.get(i).text());

            boolean is8080=is8080(port);
            if(is8080) {
                if(isHostConnectable(host,port)) {
                    ProxyEntity entity = new ProxyEntity();
                    entity.setHost(host);
                    entity.setPort(port);
                    proxyListTemp.add(entity);
                }
            }else{
                continue;
            }
        }

        if(proxyListTemp.size()>0) {
           IpProxyManager.addProxys(proxyListTemp);
            logger.info("get " + proxyListTemp.size() + " proxy Ip success");
        }
    }

    public boolean is8080(int port){
        if(port==8080)
            return true;
        return false;
    }

    public  boolean isHostConnectable(String host, int port) {
        boolean isConnectAble=true;
        Socket socket = new Socket();
        try {
            socket.setKeepAlive(false);
            socket.connect(new InetSocketAddress(host, port),1000);
        } catch (Exception e) {
           // e.printStackTrace();
            isConnectAble=false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isConnectAble;
    }

}

