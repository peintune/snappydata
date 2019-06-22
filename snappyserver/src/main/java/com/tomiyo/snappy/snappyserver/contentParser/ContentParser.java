package com.tomiyo.snappy.snappyserver.contentParser;

import com.tomiyo.snappy.snappyserver.httpUtil.HtmlunitClient;
import com.tomiyo.snappy.snappyserver.httpUtil.HttpSender;
import com.tomiyo.snappy.snappyserver.message.*;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.snappierentity.XmlNode;
import com.tomiyo.snappy.snappyserver.util.Configparser;
import com.tomiyo.snappy.snappyserver.xsoup.Xsoup;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by I322353 on 8/7/2016.
 */
public class ContentParser  implements Runnable{
    Logger logger = Logger.getLogger(ContentParser.class);

    public String name="";
    private boolean isBusy=false;
    @Override
    public void run()
    {

        while(true){
               paseOneUrlMessage();
           // System.out.println(Thread.currentThread().getName()+" parse content");
                if(!isBusy){
                    try {
                        Thread.currentThread().sleep(60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

        }

    }


    private  void paseOneUrlMessage(){
        if(!URLQueueManager.hasMessage()){
            this.isBusy=false;
            return;
        }

       // System.out.println(new Date() +"当前内容爬取线程"+Thread.currentThread().getName()+"开始爬取一条内容");
        this.isBusy=true;

        URLMessage url=URLQueueManager.fetchOne();

        //HtmlunitClient httpClient = HttpSenderManager.getHtmlUnitClient(true);
        HtmlunitClient httpClient = new HtmlunitClient(true);

        String htmlMainString= null;
        try {
            htmlMainString = httpClient.doGet(url.getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // String htmlMainString=doGet(url.getURL(),1,httpClient);
        //HttpSenderManager.returnHtmlUnitClient(true,httpClient);

        SnappierXmlEntity snappierXmlEntity=url.getXmlEntity();

        List<String> invalidContents=snappierXmlEntity.getInvalidUrlContent();
        boolean isValidUrl=true;
        for(String invalidContent:invalidContents){
            if(htmlMainString.contains(invalidContent))isValidUrl=false;
        }
        if(isValidUrl) {
            Document docTmp = Jsoup.parse(htmlMainString);

            List<String> columnResult = new ArrayList<>();
            try {
                parseHtmlByXmlNode(snappierXmlEntity.getContentUrlListNode(), docTmp, columnResult);//爬虫开始执行

            } catch (Exception e) {

            }
            if (null != columnResult && columnResult.size() > 0) {
                ContentMessage contentMessage = new ContentMessage();
                contentMessage.setColumnResult(columnResult);
                contentMessage.setSnappierXmlEntity(snappierXmlEntity);
                contentMessage.setUrlLinkAddress(url.getURL());
                contentMessage.setValide(true);
                pushOneMessage(contentMessage);
                //uSystem.out.println(new Date()+"  成功从 "+snappierXmlEntity.getTableName()+" 爬取一条数据并保存");
            }
        }else{
            ContentMessage contentMessage = new ContentMessage();
            contentMessage.setUrlLinkAddress(url.getURL());
            contentMessage.setSnappierXmlEntity(snappierXmlEntity);
            contentMessage.setValide(false);
            pushOneMessage(contentMessage);
        }
    }


    public String doGet(String url,int countnumber,HtmlunitClient httpClient){
        int count=countnumber;
        if(count==5){
            logger.warn("try to get "+url +" failed with "+count+" times");
            return "";
        }
        String htmlMainstring="";
        try {
            htmlMainstring= httpClient.doGet(url);
        }catch (Exception e){
            count++;
         htmlMainstring= doGet(url,count,httpClient);
        }
        if(htmlMainstring!=""){
            return htmlMainstring;
        }else{
            htmlMainstring= doGet(url,count,httpClient);
        }
        return htmlMainstring;
    }
    public void pushOneMessage(IMessage message){
        MessageManager.addOneMessage(message);
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
                    if(null!=singleResult)singleResult=singleResult.replace("'","<quote>");
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
                    if(null!=singleColumnResult)singleColumnResult=singleColumnResult.replace("'","<quote>");

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


    public boolean isBusy(){
        return isBusy;
    }
}
