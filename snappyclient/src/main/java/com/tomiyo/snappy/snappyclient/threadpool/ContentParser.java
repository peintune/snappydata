package com.tomiyo.snappy.snappyclient.threadpool;

import com.tomiyo.snappy.snappyclient.httpUtil.HtmlunitClient;
import com.tomiyo.snappy.snappyclient.httpUtil.HttpSender;
import com.tomiyo.snappy.snappyclient.message.ContentMessage;
import com.tomiyo.snappy.snappyclient.message.IMessage;
import com.tomiyo.snappy.snappyclient.message.MessageManager;
import com.tomiyo.snappy.snappyclient.message.URLMessage;
import com.tomiyo.snappy.snappyclient.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyclient.snappierentity.XmlNode;
import com.tomiyo.snappy.snappyclient.util.Configparser;
import com.tomiyo.snappy.snappyclient.xsoup.Xsoup;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I322353 on 8/7/2016.
 */
public class ContentParser  implements Runnable{
    Logger logger = Logger.getLogger(ContentParser.class);

    public String name="";
    private boolean isBusy=false;
    URLMessage urlMessage=null;
    String proxyhost="";
    int proxyport=8080;

    boolean clientProxy=Boolean.parseBoolean(System.getProperty("isAutoProxyIP"));
    @Override
    public void run()
    {
        paseOneUrlMessage();
        ContentParserManager.decreaseCountOfContentParser();
    }

    public ContentParser(){}
    public ContentParser(URLMessage urlMessage){
        this.urlMessage=urlMessage;
    }

    private  void paseOneUrlMessage(){

        this.isBusy=true;

        URLMessage url=urlMessage;
        SnappierXmlEntity snappierXmlEntity=url.getXmlEntity();
        boolean isautoproxy = snappierXmlEntity.isIsautoproxy();
        HtmlunitClient httpClient;

        boolean isFree = true;
        if(isFree){
            isautoproxy = false;
        }


        if(!clientProxy||!isautoproxy) {
             httpClient = new HtmlunitClient(true);
        }else{
            if(proxyhost==""){
              String[] proxyarry=  getOneProxy(snappierXmlEntity.getProxygroupname()).split(":");
              if (proxyarry.length>=2){
                  proxyhost = proxyarry[0];
                  proxyport = Integer.parseInt(proxyarry[1]);
              }else{
                  logger.error("get proxy error");
              }
            }

            httpClient = new HtmlunitClient(true,proxyhost,proxyport,false);
        }
        String htmlMainString= null;
        boolean isValidUrl=true;

        try {

            htmlMainString = httpClient.doGet(url.getURL());

            /**
             * 判断是否是错误的URL
             */
            List<String> invalidContents=snappierXmlEntity.getInvalidUrlContent();
            for(String invalidContent:invalidContents){
                if(invalidContent.contains("&&")){
                    boolean nothitinvalide=true;
                    String[] array= invalidContent.split("&&");
                    for(String content:array){
                        if(htmlMainString.contains(content))
                        {
                            nothitinvalide=nothitinvalide&true;
                        }else{
                            nothitinvalide=nothitinvalide&false;
                            break;
                        }
                    }
                    if(nothitinvalide){
                        isValidUrl=false;
                        break;
                    }
                }
                else if(htmlMainString.contains(invalidContent))
                {
                    isValidUrl=false;
                    break;
                }
            }

            /**
             * 判断是否遇到验证码
             */
            boolean isHitIdentifycode= false;
            if(isValidUrl&&isautoproxy){
                List<String> identifycode =  snappierXmlEntity.getIdentifycode();
                for(String code:identifycode){
                    if(htmlMainString.contains(code)|| htmlMainString.contains("squid")){
                        isHitIdentifycode = true;
                    }
                }
                if(isHitIdentifycode){
                    if(clientProxy&&isautoproxy)
                    postproxyResult(proxyhost+":"+proxyport,snappierXmlEntity.getProxygroupname(),"fail");
                    logger.warn("identify code ip :"+proxyhost);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("failed to request url: "+url.getURL());
        }finally {
            httpClient.close();
        }

        int failedTimes=Integer.parseInt(System.getProperty("failedTimes"));
        int contentParserNumber=Integer.parseInt(System.getProperty("contentParserNumber"));


        if(htmlMainString.equals("")||failedTimes>=contentParserNumber){
            if(clientProxy&&isautoproxy)
                postproxyResult(proxyhost+":"+proxyport,snappierXmlEntity.getProxygroupname(),"fail");
            logger.warn("null return value code ip :"+proxyhost);
            return;
        }

        boolean isrequestsucess=false;
        List<String> sucessflag=snappierXmlEntity.getRequestsuccessflag();
        if(null!=sucessflag&&sucessflag.size()>0){
            for(String success:sucessflag){
                if(htmlMainString.contains(success)){
                    isrequestsucess = true;
                    break;
                }
            }
        }

        if(!isrequestsucess){
            if(clientProxy&&isautoproxy)
                postproxyResult(proxyhost+":"+proxyport,snappierXmlEntity.getProxygroupname(),"fail");
            logger.warn("request failed code ip :"+proxyhost);
            return;
        }

        if(htmlMainString.contains("requested URL could not be retrieved")||htmlMainString.contains("403 Forbidden")){
            if(clientProxy&&isautoproxy)
            postproxyResult(proxyhost+":"+proxyport,snappierXmlEntity.getProxygroupname(),"fail");
            logger.warn("retrieved ip :"+proxyhost);

            return;
        }
        if(isValidUrl) {
            if(clientProxy&&isautoproxy)
            postproxyResult(proxyhost+":"+proxyport,snappierXmlEntity.getProxygroupname(),"success");
           // logger.warn("good ip :"+proxyhost);

            Document docTmp = Jsoup.parse(htmlMainString);
            List<String> columnResult = new ArrayList<>();
            try {
                parseHtmlByXmlNode(snappierXmlEntity.getContentUrlListNode(), docTmp, columnResult);//爬虫开始执行

            } catch (Exception e) {
e.printStackTrace();
            }finally {
                docTmp =null;
            }

            if (null != columnResult && columnResult.size() > 0) {
                ContentMessage contentMessage = new ContentMessage();
                contentMessage.setColumnResult(columnResult);
                contentMessage.setSnappierXmlEntity(snappierXmlEntity);
                contentMessage.setUrlLinkAddress(url.getURL());
                contentMessage.setValide(true);
                pushOneMessage(contentMessage);
            }
        }else{
            if(clientProxy&&isautoproxy)
                postproxyResult(proxyhost+":"+proxyport,snappierXmlEntity.getProxygroupname(),"failed");

            logger.warn("bad ip :"+proxyhost);
            ContentMessage contentMessage = new ContentMessage();
            contentMessage.setUrlLinkAddress(url.getURL());
            contentMessage.setSnappierXmlEntity(snappierXmlEntity);
            contentMessage.setValide(false);
            pushOneMessage(contentMessage);
        }
    }

    public  String getOneProxy(String groupname){
        String url="";
        if(null!=groupname&&groupname.trim()!=""){
             url = Configparser.getInstance().getProxyiphost()+"/proxyip/rest/message/getoneproxyip/"+groupname;
        }else{
             url = Configparser.getInstance().getProxyiphost()+"/proxyip/rest/message/getoneproxyip/null";
        }
        String result="";
        try {
            result= HttpSender.doGet4HttpClient(url,false).trim();

            //result =   httpClient.doGet(url);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }finally {

        }
        return result;
    }

    public void postproxyResult(String proxyip,String groupname,String issucess){
       String url = Configparser.getInstance().getProxyiphost()+"/proxyip/rest/message/postgoodproxyip";
        String message=proxyip+":"+groupname+"-"+issucess;
        try{
            String result= HttpSender.doPostJsonRequest(url,message,"",true);
        }catch (Exception e){logger.warn(e.getMessage());}

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

                if (!dbtypeLenght .contains("text") && dbtypeLenght.contains("varchar")) {
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

                    if(!dbtypeLenght.contains("text")){
                        if(null != singleResult && singleResult.length()>Integer.parseInt(dbtypeLenght)){
                            singleResult = singleResult.substring(0,  Integer.parseInt(dbtypeLenght));
                        }
                    }
//                    if (null != singleResult && singleResult.length() > (dbtypeLenght.equals("text") ? 190000 : Integer.parseInt(dbtypeLenght))) {
//                        singleResult = singleResult.substring(0, (dbtypeLenght.equals("text") ? 190000 : Integer.parseInt(dbtypeLenght)));
//                    }
                    if(null!=singleResult)singleResult=singleResult.replace("'","<quote>");

                    columnResult.add(singleResult == null ? "" : singleResult.trim());
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
                    if(!dbtypeLenght.contains("text") ){
                        if(null != singleColumnResult && singleColumnResult.length()>Integer.parseInt(dbtypeLenght)){
                            singleColumnResult = singleColumnResult.substring(0,  Integer.parseInt(dbtypeLenght));
                        }
                    }
                    if(null!=singleColumnResult)singleColumnResult=singleColumnResult.replace("'","<quote>");
                    columnResult.add(singleColumnResult == null ? "" : singleColumnResult.trim());
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
