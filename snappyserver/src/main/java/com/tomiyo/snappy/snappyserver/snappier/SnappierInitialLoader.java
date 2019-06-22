package com.tomiyo.snappy.snappyserver.snappier;

import com.tomiyo.snappy.snappyserver.httpUtil.HtmlunitClient;
import com.tomiyo.snappy.snappyserver.httpUtil.HttpSenderManager;
import com.tomiyo.snappy.snappyserver.httpUtil.HttpSenderThread;
import com.tomiyo.snappy.snappyserver.message.URLQueueManager;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import com.tomiyo.snappy.snappyserver.xsoup.Xsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class SnappierInitialLoader extends  Snappier
{
    String name="";
    int count=0;
    List<String> ignoreParam=snappierXmlEntity.getIgnoreURLParams();
    String pageUrlList=snappierXmlEntity.getPageList();
    String fromPage=snappierXmlEntity.getFromPage();
    String maxPage=snappierXmlEntity.getMaxPage();
    List<Integer> pageCountsList= countPageNumber(pageUrlList,fromPage,maxPage);
    int from;
    int to;
    int retrycount=0;
    public void initialLoad()
    {
        initialnize();
        ExecutorService exec = Executors.newCachedThreadPool();
       List<Integer> pageCountsList= countPageNumber(pageUrlList,fromPage,maxPage);
        try{
             Stack stack= new Stack();
             List<HtmlunitClient> httpClients=HttpSenderManager.getHtmlUnitClients(pageCountsList.get(0),true);

            ArrayList<Future<String>>  results =catchURL(exec,httpClients);

            pareResult(results,stack,exec,httpClients);


            HttpSenderManager.returnHtmlUnitClient(true,httpClients);
            results.clear();
            exec.shutdown();

            if(stack.size()>0){
                sendURLsToQueue(snappierXmlEntity,stack);
            }
        }catch(Exception e){
             if(Configparser.getInstance().getDebugLog()) e.printStackTrace();
        }
    }



    public void initialnize(){
        this.name=snappierXmlEntity.getTableName();
        from=pageCountsList.get(1);
        to=pageCountsList.get(2);
        retrycount=0;
        count=0;
    }

    public ArrayList<Future<String>>  catchURL(ExecutorService exec , List<HtmlunitClient> httpClients){
        ArrayList<Future<String>>  results = new ArrayList<Future<String>>();
        for(HtmlunitClient htmlunitClient:httpClients){
            String url=pageUrlList.replace("#sequence#", from+"");
            htmlunitClient.setURL(url);
            HttpSenderThread httpSenderThread=new HttpSenderThread(htmlunitClient,url);
            results.add(exec.submit(httpSenderThread));
            from++;
            if(from>to){
                break;
            }
        }
        return  results;
    }


    public void pareResult(ArrayList<Future<String>>  results,Stack stack,ExecutorService exec , List<HtmlunitClient> httpClients){
        retrycount++;
        if(retrycount==5){
            retrycount=0;
            return;
        }
        List<String> resulHtmls=new ArrayList<>();
        for(Future<String> result:results){
            try{
                String resultHtml="";
                if(result.isDone()){
                    resultHtml= result.get();
                }else{
                    resultHtml=result.get(50,TimeUnit.SECONDS);
                }
                resulHtmls.add(resultHtml);
            }catch (Exception e){
                e.printStackTrace();
                break;
            }

        }
        if(resulHtmls.size()<pageCountsList.get(0)){
            exec.shutdownNow();
            exec=Executors.newCachedThreadPool();
            stack.clear();
            results=catchURL(exec,httpClients);
            pareResult(results,stack,exec,httpClients);
        }else{
            for(String result:resulHtmls){
                retrycount=0;
                wrapURLS(result,stack);
            }
        }
    }

    public SnappierInitialLoader(SnappierXmlEntity snappierXmlEntity){
        super(snappierXmlEntity);
    }

    public boolean wrapURLS(String htmlString,Stack stack){
        {

            Document doc = Jsoup.parse(htmlString);//用jsoup解析html

            List<String> contentUrlListXpath=snappierXmlEntity.getPageUrlXpath();
            List<String> contentUrlListAttr=snappierXmlEntity.getPageUrlAttr();

            /*
            用xpath解析当前页面的所有内容页url列表
             */
            Elements results=null;
            for (String s : contentUrlListXpath)
            {
                results = Xsoup.compile(s).evaluate(doc).getElements();
                if (null != results && !results.isEmpty() && results.size() > 0)
                {
                    break;
                }
            }

            if(results.size()<1||results==null)return true;//无需爬取

            /*
            用xpath解析当前页面的所有内容页url列表，
            请求内容页并进行解析
             */

            for(Element el:results){
                String contetRrl="";
                for(String s:contentUrlListAttr){
                    contetRrl=  el.attr(s);
                    if(null!=contetRrl&&""!=contetRrl){
                        stack.add(contetRrl);
                        break;
                    }
                }
            }
            return false;
        }
    }

    public void sendURLsToQueue(SnappierXmlEntity snappierXmlEntity,Stack<String> stackURLs){
        URLQueueManager.addBacthURLMessage(snappierXmlEntity,stackURLs);
    }

    public List<Integer> countPageNumber(String pageUrlList,String fromPage,String toPage){
        List<Integer> countList=new ArrayList<>();
        int count=0;
        int from=0;
        int to=0;
        boolean start=false;
        for(int i=0;;i++){
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
        }
        countList.add(count);
        countList.add(from);
        countList.add(to);
        return countList;
    }
}
