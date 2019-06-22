package com.tomiyo.snappy.snappyserver.snappier;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Stack;


import com.tomiyo.snappy.snappyserver.httpUtil.HttpSender;
import com.tomiyo.snappy.snappyserver.message.IMessage;
import com.tomiyo.snappy.snappyserver.message.Message;
import com.tomiyo.snappy.snappyserver.snappierentity.XmlNode;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import com.tomiyo.snappy.snappyserver.util.SimFeatureUtil;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.tomiyo.snappy.snappyserver.message.MessageManager;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.xsoup.Xsoup;


public class Snappier implements Runnable
{
     Logger logger = Logger.getLogger(Snappier.class);

    String name="";
    SnappierXmlEntity snappierXmlEntity=null;
    StringBuilder lastRecord=new StringBuilder();
    int count=1;
    private boolean isFirstRecord=true;
    @Override
    public void run()
    {
        // TODO Auto-generated method stub
       // String theadname=Thread.currentThread().getName();

        initialnize();
        String pageUrlList=snappierXmlEntity.getPageList();
         try{
            int pageNumber=1;
             //pageNumber=34;
             Stack stack= new Stack();
            isFirstRecord=true;
            int counst=1;
            //while(true){
             while(counst++==1){
                String pageListUrl=pageUrlList.replace("#sequence#", pageNumber+"");
                String htmlResult="";

                try {
                     htmlResult = HttpSender.doGet(pageListUrl);
                    if(htmlResult.length()<800){
                        int time=1;
                        for(int i=0;i<15;i++){
                            Thread.currentThread().sleep(time*1000);
                            time=time+2;

                            try {
                                htmlResult = HttpSender.doGet(pageListUrl);
                                if(null!=htmlResult&&htmlResult.length()>800)
                                    break;
                            }catch (Exception e4){
                                logger.error(e4.getCause());
                            }
                        }
                    }
                }catch (Exception e){
                       int time=1;
                        for(int i=0;i<15;i++){
                            Thread.currentThread().sleep(time*1000);
                            time=time+2;
                            try {
                                htmlResult = HttpSender.doGet(pageListUrl);
                                if(null!=htmlResult)
                                break;
                            }catch (Exception e2){
                                logger.error(e2.getCause());
                            }
                        }
                }
                    pageNumber++;
                boolean isFinish=getContents(htmlResult,stack);
                if(isFinish)break;

            }
            if(null!=stack&&!stack.isEmpty()){
            Message message=new Message();
            message.setColumns(snappierXmlEntity.getContentUrlListNode());
            message.setTablename(snappierXmlEntity.getTableName());
            message.setValues(stack);
            pushOneMessage(message);
                logger.info("100% finish ："+ this.name);
            }
        }catch(Exception e){
             if(Configparser.getInstance().getDebugLog()) e.printStackTrace();
        }
    }
    public Snappier(SnappierXmlEntity snappierXmlEntity){
        this.snappierXmlEntity= snappierXmlEntity;
        this.name= this.snappierXmlEntity.getTableName();
    }

    public boolean getContents(String htmlString,Stack stack){
        {

            Document doc = Jsoup.parse(htmlString);//用jsoup解析html

            List<String> contentUrlListXpath=snappierXmlEntity.getPageUrlXpath();
            List<String> contentUrlListAttr=snappierXmlEntity.getPageUrlAttr();

            /*
            用xpath解析当前页面的所有内容页url列表
             */
            Elements results=null;
            int counts=1;
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
                        break;
                    }
                }

                String htmlMainString="";
                try{
                    try {
                        htmlMainString = HttpSender.doGet(contetRrl == null ? "" : contetRrl);
                        if(htmlMainString.length()<800){
                            int time=1;
                            for(int i=0;i<15;i++){
                                Thread.currentThread().sleep(time*1000);
                                time=time+2;
                                try {
                                    htmlMainString = HttpSender.doGet(contetRrl);
                                    if(null!=htmlMainString&&htmlMainString.length()>800)
                                        break;
                                }catch (Exception e4){
                                    if(Configparser.getInstance().getDebugLog())  e4.printStackTrace();
                                }
                            }
                        }
                    }catch (Exception e1){
                        int time=1;
                        for(int i=0;i<15;i++){
                            Thread.currentThread().sleep(time*1000);
                            time=time+2;
                            try {
                                htmlMainString = HttpSender.doGet(contetRrl == null ? "" : contetRrl);
                                if(null!=htmlMainString)
                                    break;
                            }catch (Exception e2){
                                if(Configparser.getInstance().getDebugLog()) e2.printStackTrace();
                            }
                        }
                    }

                    Document docTmp=Jsoup.parse(htmlMainString);

                    List<String> columnResult=new ArrayList<>();

                    parseHtmlByXmlNode(snappierXmlEntity.getContentUrlListNode(),docTmp,columnResult);//爬虫开始执行

                    if(isFirstRecord){
                        StringBuilder  sb=new StringBuilder();
                        for(String colum:columnResult){
                            sb.append(colum+"#&#");
                        }
                        String latestRecordString="";
                        if(lastRecord.length()>1){
                            latestRecordString=lastRecord.toString();
                        }
                        else{
                            latestRecordString=MysqlHadler.getInstance().queryLatestRecord(snappierXmlEntity.getTableName(),snappierXmlEntity.getContentUrlListNode());
                        }
                        //比较
                        if(latestRecordString.equals(sb.toString()))return true;
                        double rate= SimFeatureUtil.sim(latestRecordString, sb.toString());
                        if(rate>0.9){
                            return true;
                        }else{
                            lastRecord.delete(0, lastRecord.length());
                            lastRecord=sb;
                        }
                        isFirstRecord=false;
                        logger.info("start to run snappier: "+ this.name);
                    }
                    if(null!=columnResult&&columnResult.size()>0)
                    stack.push(columnResult);
                    counts++;
                    //if(counts==5)return true;//debug用的
                    logger.info(" crawling "+(count++)+" records success from "+ this.name);
                }catch(Exception e){
                    if(Configparser.getInstance().getDebugLog())e.printStackTrace();
                    continue;
                }

            }
            return false;
        }
    }

    private void parseHtmlByXmlNode(XmlNode xmlNode, Document doc, List<String> columnResult) throws Exception {
        if(xmlNode.type==XmlNode.NodeType.contentUrlList){
            List<XmlNode> subXmlNodes=xmlNode.getSubNodesList();
            for(XmlNode node:subXmlNodes){
                parseHtmlByXmlNode(node,doc,columnResult);
            }
        }else {
            if (xmlNode.type == XmlNode.NodeType.column) {
                String singleColumnResult = "";
                String dbtypeLenght = xmlNode.getDbtype();

                if (dbtypeLenght != "text" && dbtypeLenght.contains("varchar")) {
                    String tmp = dbtypeLenght.replace("varchar(", "");
                    dbtypeLenght = tmp.substring(0, tmp.length() - 1);
                }
                if (xmlNode.hasSubNodes()) {
                    String singleResult = "{contents:[";
                    Elements els = null;
                    String parentXpath = "";
                    for (String s : xmlNode.getXpathList()) {
                        els = Xsoup.compile(s).evaluate(doc).getElements();
                        parentXpath = s;
                        if (els != null && els.size() > 0) ;
                        break;
                    }
                    List<XmlNode> subNodes = xmlNode.getSubNodesList();

                    for (Element el : els) {
                        String jsonObjItem = "{";
                        for (XmlNode node : subNodes) {
                            if (node.type != XmlNode.NodeType.partial) continue;//目前在column节点下只能有partial节点
                            jsonObjItem += "\"" + node.getName() + "\":";
                            String resultString = "";
                            for (String xpath : node.getXpathList()) {
                                int oldLength = xpath.length();
                                String newXpathTemp = xpath.replace(parentXpath, "");
                                if (oldLength == newXpathTemp.length()) continue;
                                int position = newXpathTemp.indexOf("/");
                                if (position != -1) {
                                    newXpathTemp = newXpathTemp.substring(position);
                                }
                                if (node.getAttrList().size() > 0) {
                                   List<String> attrList= node.getAttrList();
                                    Elements elems = Xsoup.select(el, newXpathTemp).getElements();
                                    for(Element ele:elems){
                                        String tmmm="";
                                        for(String attr:attrList){
                                            tmmm=ele.attr(attr);
                                            if(tmmm!=null&&tmmm.length()>0){
                                                break;
                                            }
                                        }
                                        if(tmmm.length()>0){
                                            resultString+=tmmm+",";
                                        }
                                    }

                                } else {
                                    resultString = Xsoup.select(el, newXpathTemp).get();//拿到partial的内容
                                }
                                if (null != resultString && resultString != "") break;
                            }
                            if (null == resultString) resultString = "";
                            jsonObjItem += "\"" + resultString + "\",";
                        }
                        if (jsonObjItem.endsWith(",")) jsonObjItem = jsonObjItem.substring(0, jsonObjItem.length() - 1);
                        singleResult += jsonObjItem + "},";
                    }

                    if (singleResult.endsWith(",")) singleResult = singleResult.substring(0, singleResult.length() - 1);
                    singleResult += "]}";

                    if (null != singleResult && singleResult.length() > (dbtypeLenght.equals("text") ? 65535 : Integer.parseInt(dbtypeLenght))) {
                        singleResult = singleResult.substring(0, (dbtypeLenght.equals("text") ? 65535 : Integer.parseInt(dbtypeLenght)));
                    }
                    columnResult.add(singleResult == null ? "" : singleResult);
                } else {

                    if (xmlNode.getValue() != "" && xmlNode.getValue() != null) {
                        singleColumnResult = xmlNode.getValue();
                    } else if (xmlNode.getXpathList().size() > 0) {
                        for (String s : xmlNode.getXpathList()) {
                            if (s.contains("+")) {
                                String[] xpaths = s.split("\\+");
                                for (String xpath : xpaths) {
                                    if(!xpath.startsWith("//")){
                                        singleColumnResult += xpath;
                                        continue;
                                    }
                                    String xpathString = xpath.trim();
                                    String tmpString = "";
                                    List<String> listString = Xsoup.compile(xpathString).evaluate(doc).list();

                                    for (String ssss : listString) {
                                        if (null != ssss && ssss.length() > 0) {
                                            tmpString+= ssss + ",";
                                        }
                                    }
                                    if (null == tmpString || tmpString.equals("[]")) tmpString = "";
                                    if (tmpString.length() > 1)
                                        tmpString = tmpString.substring(0, tmpString.length() - 1);
                                    singleColumnResult += tmpString;
                                }
                                if (null != singleColumnResult && "" != singleColumnResult) break;
                            } else {
                                if(xmlNode.getAttrList().size()>0){

                                     Elements elsss = Xsoup.compile(s).evaluate(doc).getElements();

                                    for(Element elssssd:elsss ){
                                        List<String> attrList=xmlNode.getAttrList();
                                        String tmpString="";
                                         for(String ssfds:attrList){
                                             tmpString=elssssd.attr(ssfds);
                                             if(null!=tmpString&&tmpString.length()>0)break;
                                         }
                                        if(tmpString.length()>0)
                                        singleColumnResult+=tmpString+",";
                                    }

                                }else {
                                    List<String> listString = Xsoup.compile(s).evaluate(doc).list();
                                    for (String ssss : listString) {
                                        if (null != ssss && ssss.length() > 0) {
                                            singleColumnResult += ssss + ",";
                                        }
                                    }
                                }

                                // singleColumnResult = Xsoup.compile(s).evaluate(doc).list().toString();
                                if (singleColumnResult.length() > 1)
                                    singleColumnResult = singleColumnResult.substring(0, singleColumnResult.length() - 1);
                                if (null != singleColumnResult && "" != singleColumnResult) break;
                            }
                        }
                    }
                    // System.out.println(singleColumnResult);
                    if (null != singleColumnResult && singleColumnResult.length() > (dbtypeLenght.equals("text") ? 65535 : Integer.parseInt(dbtypeLenght))) {
                        singleColumnResult = singleColumnResult.substring(0, (dbtypeLenght.equals("text") ? 65535 : Integer.parseInt(dbtypeLenght)));
                    }
                    columnResult.add(singleColumnResult == null ? "" : singleColumnResult);
                }
            } else if (xmlNode.type == XmlNode.NodeType.url) {
                if (!xmlNode.hasSubNodes()) return;
                Element url = null;
                String contentUrl = "";
                for (String s : xmlNode.getXpathList()) {
                    try {
                        url = Xsoup.compile(s).evaluate(doc).getElements().get(0);
                    } catch (Exception e) {
                        if(Configparser.getInstance().getDebugLog()) e.printStackTrace();
                    }
                    if (null != url) break;
                }
                Document docTmp = null;
                if (url == null) {
                    //return;
                } else {
                    for (String s : xmlNode.getAttrList()) {
                        contentUrl = url.attr(s);
                        if (null != contentUrl && "" != contentUrl) break;
                    }
                    String htmlMainString = null;
                    try {
                        htmlMainString = HttpSender.doGet(contentUrl);
                        int time = 1;
                        for (int i = 0; i < 15; i++) {
                            Thread.currentThread().sleep(time*1000);
                            time = time + 2;
                            try {
                                htmlMainString = HttpSender.doGet(contentUrl);
                                if (null != htmlMainString && htmlMainString.length() > 800)
                                    break;
                            } catch (Exception e4) {
                                if(Configparser.getInstance().getDebugLog())e4.printStackTrace();
                            }
                        }
                    } catch (Exception e1) {
                        int time = 1;
                        for (int i = 0; i < 15; i++) {
                            Thread.currentThread().sleep(time*1000);
                            time = time + 2;
                            try {
                                htmlMainString = HttpSender.doGet(contentUrl);
                                break;
                            } catch (Exception e2) {
                                if(Configparser.getInstance().getDebugLog())e2.printStackTrace();
                            }
                        }
                        //e1.printStackTrace();
                    }
                    docTmp = Jsoup.parse(htmlMainString);
                }
                List<XmlNode> subNodes = xmlNode.getSubNodesList();
                for (XmlNode node : subNodes) {
                    parseHtmlByXmlNode(node, docTmp, columnResult);
                }

            }
        }
    }

    public Snappier(String name){
        this.name=name;
    }
    public String getName(){
        return this.name;
    }

    public void initialnize(){
        this.name=snappierXmlEntity.getTableName();
        
    }
    public void pushOneMessage(IMessage message){
        MessageManager.addOneMessage(message);
    }
    public void stop(){
        
    }
    
    public void susbend(){
        
    }
}
