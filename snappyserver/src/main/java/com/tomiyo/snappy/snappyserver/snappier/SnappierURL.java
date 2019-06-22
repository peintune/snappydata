package com.tomiyo.snappy.snappyserver.snappier;

import com.tomiyo.snappy.snappyserver.httpUtil.HtmlunitClient;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import com.tomiyo.snappy.snappyserver.httpUtil.HttpSenderThread;
import com.tomiyo.snappy.snappyserver.ipproxy.IpProxyManager;
import com.tomiyo.snappy.snappyserver.message.URLQueueManager;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.util.HttpProxyIp;
import com.tomiyo.snappy.snappyserver.xsoup.Xsoup;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.net.www.http.HttpClient;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.*;


public class SnappierURL extends  Snappier implements Runnable
{
    Logger logger = Logger.getLogger(SnappierURL.class);

    String name="";
    int count=0;
    List<String> ignoreParam=snappierXmlEntity.getIgnoreURLParams();
    String pageUrlList=snappierXmlEntity.getPageList();
    String fromPage=snappierXmlEntity.getFromPage();
    String toPage=snappierXmlEntity.getToPage();
    List<Integer> pageCountsList= null;
    boolean autoProxy=snappierXmlEntity.isIsautoproxy();
    boolean initialLoad=false;
    int from;
    int to;
    int retrycount = 0;
    boolean isPaseSuccess = true;
    int totalSize = 0;
    boolean meetIdentifyCode=false;
    List<String> identifyCodeList = snappierXmlEntity.getIdentifycode();

    public void initialLoadPages(){

        this.name=snappierXmlEntity.getTableName();
        initialLoad = true;

        pageCountsList =   countPageNumber(pageUrlList,fromPage,toPage);
        from=pageCountsList.get(1);
        to=pageCountsList.get(2);

        retrycount=0;
        count=0;
        isPaseSuccess=true;
        totalSize=0;
        meetIdentifyCode = false;

        Stack stack= new Stack();
        List<HtmlunitClient> httpClients=new ArrayList<>();

        HtmlunitClient httpcleint1 =new HtmlunitClient(true, null, 0, true);
        httpClients.add(httpcleint1);

        pareResult(stack,httpClients);

        if(stack.size()>0){
            if(stack.size()==totalSize){
                logger.warn("too much data from"+ snappierXmlEntity.getTableName()+", please consider a shorter time cycle");
            }
            sendURLsToQueue(snappierXmlEntity,stack);
        }else{
            if(retrycount>=3){
                logger.error( "get URL failed from "+snappierXmlEntity.getTableName()+" the url is: "+fromPage);
            }else {
                logger.warn("no data for recently from " + snappierXmlEntity.getTableName() + ", please consider a longer time cycle");
            }
        }

        for(HtmlunitClient htmlunitClient:httpClients){
            htmlunitClient.close();
        }

    }


    @Override
    public void run()
    {
        initialnize();

        try{
            Stack stack= new Stack();
            List<HtmlunitClient> httpClients=new ArrayList<>();
            for(int i=0;i<pageCountsList.get(0);i++){
                httpClients.add(new HtmlunitClient(true, null, 0, true));
            }

            pareResult(stack,httpClients);

            if(stack.size()>0){
                if(stack.size()==totalSize){
                    logger.warn("too much data from"+ snappierXmlEntity.getTableName()+", please consider a shorter time cycle");
                }
                sendURLsToQueue(snappierXmlEntity,stack);
            }else{
                if(retrycount>=3){
                    logger.error( "get URL failed from "+snappierXmlEntity.getTableName()+" the url is: "+fromPage);
                }else {
                    logger.warn("no data for recently from " + snappierXmlEntity.getTableName() + ", please consider a longer time cycle");
                }
            }

            for(HtmlunitClient htmlunitClient:httpClients){
                htmlunitClient.close();
            }
            updateStartTime();

        }catch(Exception e){
             if(Configparser.getInstance().getDebugLog()) logger.error(e.getMessage());
        }
    }

    private  void updateStartTime(){
        long delayTime=snappierXmlEntity.getSleepTime()*1000;
        Date date = new Date();
        Timestamp timeStamp = new Timestamp(date.getTime()+delayTime);
        String sql="update snappiers set first_starttime='"+timeStamp+"' where snappiername='"+snappierXmlEntity.getTableName()+"'";
        MysqlHadler.getInstance().executeUpdateSql(sql);
    }


    public ArrayList<FutureTask<String>>  catchURL(List<HtmlunitClient> httpClients){
        ArrayList<FutureTask<String>>  results = new ArrayList<FutureTask<String>>();
        for(HtmlunitClient htmlunitClient:httpClients){
                String url=pageUrlList.replace("#sequence#", from+"");
                HttpSenderThread httpSenderThread=new HttpSenderThread(htmlunitClient,url);
                FutureTask<String> futureTask = new FutureTask<String>(httpSenderThread);
                 Thread thread = new Thread(futureTask);
                 thread.start();
                 results.add(futureTask);
            from++;
                if(from>to){
                    break;
                }
        }
        return  results;
    }


    public void pareResult(Stack stack, List<HtmlunitClient> httpClients) {

        from = pageCountsList.get(1);
        to = pageCountsList.get(2);

        if (retrycount >2) {
            logger.error("can not get URL data after try 3 times  from "+snappierXmlEntity.getName());
            return;
        }
        if (autoProxy && meetIdentifyCode && retrycount > 0) {
            String proxyHost = null;
            int proxyPort = 0;
            try {
                String[] proxyarry= HttpProxyIp.getOneBestProxy(snappierXmlEntity.getProxygroupname()).split(":");
                if (proxyarry.length>=2){
                    proxyHost = proxyarry[0];
                    proxyPort = Integer.parseInt(proxyarry[1]);
                }
            } catch (Exception e) {
            }
            for (HtmlunitClient htmlunitClient : httpClients) {
                if (proxyHost == null || proxyHost.contains("no data")) {
                    htmlunitClient.reinit(true, null, proxyPort, true);//不用代理爬取
                } else {
                    htmlunitClient.reinit(true, proxyHost, proxyPort, false);//用代理爬取
                }
            }
        }


        List<String> resulHtmls = new ArrayList<>();


        if(initialLoad) {
            for (int i = from; i <= to; i++) {
                String url = pageUrlList.replace("#sequence#", i + "");
                String resltOne = "";
                try {
                    resltOne = httpClients.get(0).doGet(url);
                    //判断是否遇到验证码
                    boolean meetIdentifyCodeThistime = false;
                    if (autoProxy) {
                        for (String identifycode : identifyCodeList) {
                            if (resltOne.contains(identifycode)) {
                                meetIdentifyCode = true;
                                meetIdentifyCodeThistime = true;
                                break;
                            }
                        }
                    }
                    if (!meetIdentifyCodeThistime) {
                        resulHtmls.add(resltOne);
                    }
                    resulHtmls.add(resltOne);

            }catch(Exception eee){
                    logger.warn("initial load failed "+url+"   "+ eee.getMessage());
            }
        }

        }else{
            ArrayList<FutureTask<String>> results = catchURL(httpClients);
            for (Future<String> result : results) {
                try {
                    String resultHtml = "";
                    resultHtml = result.get();

                    //判断是否遇到验证码
                    boolean meetIdentifyCodeThistime = false;
                    if(autoProxy) {
                        for (String identifycode : identifyCodeList) {
                            if (resultHtml.contains(identifycode)) {
                                meetIdentifyCode = true;
                                meetIdentifyCodeThistime= true;
                                break;
                            }
                        }
                    }
                    if(!meetIdentifyCodeThistime) {
                        resulHtmls.add(resultHtml);
                    }
                } catch (Exception e) {
                    //logger.error(e.getMessage());
                    break;
                }
            }
        }


        if(resulHtmls.size()>0 && resulHtmls.size()<pageCountsList.get(0)){
            logger.warn("not get the full data, the expect page number is "+pageCountsList.get(0)+", but get "+resulHtmls.size()+" pages. From  "+snappierXmlEntity.getName());
          }


       int failedtimes = 0;

       if(resulHtmls.size()==0){
            stack.clear();
           retrycount++;
            pareResult(stack,httpClients);
        }else{
            for(String result:resulHtmls){
                boolean isSuccess=wrapURLS(result,stack);
                if(!isSuccess){
                    failedtimes++;
                    //isPaseSuccess=false;
                    //break;
                }
            }
        }

        if(resulHtmls.size()!=0 && failedtimes>=resulHtmls.size()){
            stack.clear();
            retrycount++;
            pareResult(stack,httpClients);
        }
    }
    public SnappierURL(SnappierXmlEntity snappierXmlEntity){
        super(snappierXmlEntity);
    }

    public boolean wrapURLS(String htmlString,Stack stack){
        {

            Document doc = Jsoup.parse(htmlString);

            List<String> contentUrlListXpath=snappierXmlEntity.getPageUrlXpath();
            List<String> contentUrlListAttr=snappierXmlEntity.getPageUrlAttr();
            List<String> contentUrlAppendBefore=snappierXmlEntity.getContentUrlAppendBefore();
            List<String> contentUrlAppendAfter=snappierXmlEntity.getContentUrlAppendAfter();

            /*
            用xpath解析当前页面的所有内容页url列表
             */
            Elements results=null;
            for (String s : contentUrlListXpath)
            {
                try {
                    results = Xsoup.compile(s).evaluate(doc).getElements();
                }catch (Exception eeee){
                    logger.warn("Failed to pars xpath "+s+ "        "+ eeee.getMessage());
                    continue;
                }
                if (null != results && !results.isEmpty() && results.size() > 0)
                {
                    break;
                }
            }

            if(null==results || results.size()<1)return false;//无需爬取

            /*
            用xpath解析当前页面的所有内容页url列表，
            请求内容页并进行解析
             */

            totalSize+=results.size();
            for(Element el:results){
                String contetRrl="";
                for(String s:contentUrlListAttr){
                    contetRrl=  el.attr(s);
                    if(null!=contetRrl&&""!=contetRrl){
                       // if(!isURLExist(contetRrl)) {
                        if(contentUrlAppendBefore.size()>0 || contentUrlAppendAfter.size()>0){
                            String before="";
                            for(String appendBefore:contentUrlAppendBefore){
                                before=before+appendBefore;
                            }

                            String after="";

                            for(String appendafter:contentUrlAppendAfter){
                                after=after+appendafter;
                            }
                            if(before!=""){
                                contetRrl=before+contetRrl;
                            }
                            if(after!=""){
                                contetRrl=contetRrl+after;
                            }
                        }
                        if(!stack.contains(contetRrl)){
                            stack.add(contetRrl);
                            count++;
                        }
                        break;
                    }
                }
            }
            return true;
        }
    }

    public String getName(){
        return this.name;
    }

    public void initialnize(){
        this.name=snappierXmlEntity.getTableName();
        pageCountsList =   countPageNumber(pageUrlList,fromPage,toPage);

        from=pageCountsList.get(1);
        to=pageCountsList.get(2);
        retrycount=0;
        count=0;
        isPaseSuccess=true;
        totalSize=0;
        meetIdentifyCode = false;
    }

    public void sendURLsToQueue(SnappierXmlEntity snappierXmlEntity,Stack<String> stackURLs){
        URLQueueManager.addBacthURLMessage(snappierXmlEntity,stackURLs);
    }

    public boolean isURLExist(String url){
        String newURL=recutURL(url);

        boolean isExist=false;
        try {
            ResultSet resultset= MysqlHadler.getInstance().executeQuerySql("select count(*) from "+name+" where urlLinkAddress like '"+newURL+"%'");
            resultset.next();
            if (resultset.getInt(1)>0){
                isExist=true;
            }
            try{
                resultset.getStatement().close();
                resultset.close();
            }catch (Exception e2){}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isExist;
    }

    public List<Integer> countPageNumber(String pageUrlList,String fromPage,String toPage){
        List<Integer> countList=new ArrayList<>();
        if(initialLoad){
            toPage = snappierXmlEntity.getMaxPage();
        }
        int count=0;
        int from=0;
        int to=0;
        boolean start=false;
        for(int i=0;i<1000;i++){
            String url=pageUrlList.replace("#sequence#", i+"");
            if(url.equalsIgnoreCase(fromPage)){
                from=i;
                start=true;
            }
            if(start){
                count++;
            }
            if(url.equalsIgnoreCase(toPage)){
                to=i;
                start=false;
                break;
            }
            if(i>=1000){
                logger.error(snappierXmlEntity.getTableName()+" generate url failed "+url+" from:" +fromPage+" to:"+toPage);
                logger.error(snappierXmlEntity.getTableName()+" too much pages,the max pages number is 500 ");
                to= from;
                break;
            }
        }
        countList.add(count);
        countList.add(from);
        countList.add(to);
        return countList;
    }

    public String recutURL(String url){
        if(null==ignoreParam||ignoreParam.size()==0)return url;
        String[] params=url.split("&");
        String resultURL="";
        for(String param:params){
            boolean ignore=false;
            for(String ignorePara:ignoreParam) {
                if (param.trim().startsWith(ignorePara))ignore=true;
            }
            if(!ignore){
                resultURL+=param+"&";
            }
        }
        return resultURL;
    }

}
