package com.tomiyo.snappy.snappyserver.snappierentity;

import com.tomiyo.snappy.snappyserver.util.XmlFileParser;

import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by hekun on 16/6/23.
 */
public class SnappierXmlEntity implements ISnappier{
    public String filename=null;
    public String pageList="";

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setPageList(String pageList) {
        this.pageList = pageList;
    }

    public void setMaxPage(String maxPage) {
        this.maxPage = maxPage;
    }

    public void setFromPage(String fromPage) {
        this.fromPage = fromPage;
    }

    public void setToPage(String toPage) {
        this.toPage = toPage;
    }

    public List<String> getContentUrlListXpath() {
        return contentUrlListXpath;
    }

    public void setContentUrlListXpath(List<String> contentUrlListXpath) {
        this.contentUrlListXpath = contentUrlListXpath;
    }

    public List<String> getContentUrlListAttr() {
        return contentUrlListAttr;
    }

    public void setContentUrlListAttr(List<String> contentUrlListAttr) {
        this.contentUrlListAttr = contentUrlListAttr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumnsRule(HashMap<String, List<String>> columnsRule) {
        this.columnsRule = columnsRule;
    }

    public void setContentUrlListNode(XmlNode contentUrlListNode) {
        this.contentUrlListNode = contentUrlListNode;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
    }

    public void setIgnoreURLParams(List<String> ignoreURLParams) {
        this.ignoreURLParams = ignoreURLParams;
    }

    public void setInvalidUrlContent(List<String> invalidUrlContent) {
        this.invalidUrlContent = invalidUrlContent;
    }

    public int getSleeptime() {
        return sleeptime;
    }

    public void setSleeptime(int sleeptime) {
        this.sleeptime = sleeptime;
    }

    public String maxPage="";
    public String fromPage="";
    public String toPage="";
    public List<String> contentUrlListXpath =new ArrayList<>();
    public List<String> contentUrlListAttr=new ArrayList<>();

    public List<String> getContentUrlAppendBefore() {
        return contentUrlAppendBefore;
    }

    public void setContentUrlAppendBefore(List<String> contentUrlAppendBefore) {
        this.contentUrlAppendBefore = contentUrlAppendBefore;
    }

    public List<String> getContentUrlAppendAfter() {
        return contentUrlAppendAfter;
    }

    public void setContentUrlAppendAfter(List<String> contentUrlAppendAfter) {
        this.contentUrlAppendAfter = contentUrlAppendAfter;
    }

    public List<String> contentUrlAppendBefore=new ArrayList<>();
    public List<String> contentUrlAppendAfter=new ArrayList<>();

    public String name="";
    public String folderPath="";
    public HashMap<String, List<String>> columnsRule=new HashMap<>();
    public  XmlNode contentUrlListNode=null;
    public List<String> columnList=new ArrayList<>();
    public List<String> ignoreURLParams=new ArrayList<>();
    public List<String> invalidUrlContent=new ArrayList<>();

    public List<String> getRequestsuccessflag() {
        return requestsuccessflag;
    }

    public void setRequestsuccessflag(List<String> requestsuccessflag) {
        this.requestsuccessflag = requestsuccessflag;
    }

    public List<String> requestsuccessflag=new ArrayList<>();


    public boolean isautoproxy=false;

    public List<String> getIdentifycode() {
        return identifycode;
    }

    public List<String> identifycode=new ArrayList<>();
    public int sleeptime=3600;

    public String getProxygroupname() {
        return proxygroupname;
    }

    public void setProxygroupname(String proxygroupname) {
        this.proxygroupname = proxygroupname;
    }

    public String proxygroupname="";

    public SnappierXmlEntity(String filename){
        this.filename=filename;
        initialnize();
    }

    public SnappierXmlEntity(){}
    public boolean isIsautoproxy() {
        return isautoproxy;
    }
    private synchronized void initialnize(){

        name=new File(filename).getName().split(".xml")[0];

        XmlFileParser xmlFileParser= new XmlFileParser(filename);

        sleeptime=Integer.parseInt(xmlFileParser.findElement("sleeptime").attributeValue("value"));
        Element pargerulesEl=xmlFileParser.findElement("pageRules");
        String pageList1= pargerulesEl.elements("rule").get(0).attributeValue("value");
        String pageList2= pargerulesEl.elements("rule").get(1).attributeValue("value");
        maxPage=pargerulesEl.elements("initialMaxpage").get(0).attributeValue("value");
        fromPage=pargerulesEl.element("crawlingRange").attributeValue("from");
        toPage=pargerulesEl.element("crawlingRange").attributeValue("to");
        //List<Element> columnrulesElList=xmlFileParser.findElement("columnrules").elements("rule");

        this.setPageList(pageList1, pageList2);

        Element contentUrlList=xmlFileParser.findElement("contentUrlList");

        List<Attribute> pageurlAttrs = contentUrlList.attributes();
        for(Attribute att:pageurlAttrs){
            if(att.getName().contains("xpath")){
                contentUrlListXpath.add(att.getValue());
            }else if(att.getName().contains("attr")){
                contentUrlListAttr.add(att.getValue());
            }else if(att.getName().contains("appendbefore")){
                contentUrlAppendBefore.add(att.getValue());
            }else if(att.getName().contains("appendafter")){
                contentUrlAppendAfter.add(att.getValue());
            }
        }

        Element ignoreURLEl=pargerulesEl.elements("ignoreURLParams").get(0);

        List<Attribute> ignoreAttr=ignoreURLEl.attributes();
        for(Attribute attr:ignoreAttr){
            if(attr.getName().contains("param")){
                ignoreURLParams.add(attr.getValue());
            }
        }

        Element invalidUrl=pargerulesEl.elements("invalidUrl").get(0);

        List<Attribute> invalidUrlAttr=invalidUrl.attributes();
        for(Attribute attr:invalidUrlAttr){
            if(attr.getName().contains("content")){
                invalidUrlContent.add(attr.getValue());
            }
        }


        Element requestsucessflagel=pargerulesEl.elements("requestsucessflag").get(0);

        List<Attribute> requestsucessflagAtr=requestsucessflagel.attributes();
        for(Attribute attr:requestsucessflagAtr){
            if(attr.getName().contains("content")){
                requestsuccessflag.add(attr.getValue());
            }
        }
        try {

            Element autoproxy = pargerulesEl.elements("autoproxy").get(0);

            List<Attribute> autoproxyattr = autoproxy.attributes();
            for (Attribute attr : autoproxyattr) {
                if (attr.getName().contains("identifycod")) {
                    identifycode.add(attr.getValue());
                } else if (attr.getName().contains("enable")) {
                    try {
                        isautoproxy = Boolean.parseBoolean(attr.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(attr.getName().contains("proxygroupname")){
                    proxygroupname=attr.getValue();
                }
            }
        }catch (Exception e){

        }

        List<Element> columnNode = xmlFileParser.findElements("column");

        // List<Element> contentSubElements=contentUrlList.elements();
        contentUrlListNode=new XmlNode(XmlNode.NodeType.contentUrlList);

        parseNodes(contentUrlListNode,contentUrlList);//关键步骤

        int columnLength=columnNode.size();

        for(int i=0;i<columnLength;i++){
            List<Attribute> ruleAttrs = columnNode.get(i).attributes();
            List<String> ruleXpathValue=new ArrayList<>();
            for(Attribute att:ruleAttrs){
                if(!att.getName().equalsIgnoreCase("name")&&att.getName().contains("xpath")){
                    ruleXpathValue.add(att.getValue());
                }
            }
            String columnName=columnNode.get(i).attributeValue("name");
            columnsRule.put(columnName, ruleXpathValue);
            columnList.add(columnName);
        }

        xmlFileParser.closeDocument(filename);
    }
    public String getPageList(){
        return this.pageList;
    }
    public String getMaxPage(){
        return this.maxPage;
    }

    public String getFromPage(){
        return this.fromPage;
    }

    public String getToPage(){
        return this.toPage;
    }

    private void parseNodes(XmlNode xmlNode,Element element){
        List<Attribute> attrs=element.attributes();
        setupAttrs(xmlNode,attrs);
        List<Element> subElements=element.elements();
        for (Element el : subElements) {
            XmlNode xmlSubNode = new XmlNode(getXmlNOdeType(el.getName()));
            parseNodes(xmlSubNode, el);
            xmlNode.addSubNode(xmlSubNode);
        }

    }

    public List<String> getIgnoreURLParams(){
        return  this.ignoreURLParams;
    }

    private void setupAttrs(XmlNode xmlNode,List<Attribute> attrs){
        for (Attribute attr : attrs) {
            String attrName = attr.getName();
            if (attrName.contains("name")) {
                xmlNode.setName(attr.getValue());
            } else if (attrName.contains("dbtype")) {
                xmlNode.setDbtype(attr.getValue());
            } else if (attrName.contains("xpath")) {
                xmlNode.addXpath(attr.getValue());
            } else if (attrName.contains("attr")) {
                xmlNode.addAttr(attr.getValue());
            } else if (attrName.contains("value")) {
                xmlNode.setValue(attr.getValue());
            }
        }
    }
    private XmlNode.NodeType getXmlNOdeType(String xmlNodeType){
        switch (xmlNodeType){
            case "sleeptime":return XmlNode.NodeType.sleeptime;
            case "pageRules":return XmlNode.NodeType.pageRules;
            case "rule":return XmlNode.NodeType.rule;
            case "contentUrlList":return XmlNode.NodeType.contentUrlList;
            case "column":return XmlNode.NodeType.column;
            case "url":return XmlNode.NodeType.url;
            case "partial":return XmlNode.NodeType.partial;
            default:return null;
        }
    }
    private void setPageList(String pageList1,String pageList2){
        for(int i=0;i<pageList1.length();i++){
            if(pageList1.charAt(i)!=pageList2.charAt(i)){
                pageList=pageList1.substring(0, i)+"#sequence#";
                if(i<pageList1.length()){
                    pageList+=pageList1.substring(i+1,pageList1.length());
                }
            }
        }
    }
    public List<String> getPageUrlXpath(){
        return this.contentUrlListXpath;
    }

    public List<String> getPageUrlAttr(){
        return this.contentUrlListAttr;
    }
    public HashMap<String, List<String>> getColumnsRule()
    {
        return this.columnsRule;
    }
    public List<String> getColumnList(){
        return this.columnList;
    }
    public String getTableName(){
        return this.name;
    }

    public String getFolderPath(){
        return this.folderPath;
    }

    public void setFolderPath(String path){
        this.folderPath=path;
    }
    public int getSleepTime(){
        return this.sleeptime;
    }

    public XmlNode getContentUrlListNode(){
        return this.contentUrlListNode;
    }
    public List<String> getInvalidUrlContent(){
        return this.invalidUrlContent;
    }

}
