package com.tomiyo.snappy.snappyserver.snappierentity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I322353 on 7/18/2016.
 */
public class XmlNode {

    public void setXpathList(List<String> xpathList) {
        this.xpathList = xpathList;
    }

    public void setAttrList(List<String> attrList) {
        this.attrList = attrList;
    }

    public void setSubNodesList(List<XmlNode> subNodesList) {
        this.subNodesList = subNodesList;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String name="";
    public String dbtype="varchar(50)";
    public String parsetype="value"; //如果是value，那么就保存html的值，如果是htmlnode，就保存这个node的完整的
    public String value="";
    public List<String> xpathList=new ArrayList();
    public List<String> attrList= new ArrayList();
    public List<XmlNode> subNodesList=new ArrayList<>();
    public NodeType type=NodeType.column;

    public XmlNode(){}
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<XmlNode> getSubNodesList() {
        return subNodesList;
    }

    public void addSubNode(XmlNode subNode) {
        this.subNodesList.add(subNode);
    }
    public enum NodeType {
        column, url, partial,contentUrlList,rule,pageRules,sleeptime
    }

    public XmlNode(NodeType type){
        this.type=type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbtype() {
        if(null==this.dbtype)return "varchar(50)";
        return this.dbtype;
    }

    public void setDbtype(String dbtype) {
        this.dbtype = dbtype;
    }

    public List<String> getXpathList() {
        return xpathList;
    }

    public void addXpath(String xpath) {
        this.xpathList.add(xpath);
    }

    public List<String> getAttrList() {
        return attrList;
    }

    public void addAttr(String attr) {
        this.attrList.add(attr);
    }

    public boolean hasSubNodes(){
        return this.subNodesList.size()>0;
    }
}
