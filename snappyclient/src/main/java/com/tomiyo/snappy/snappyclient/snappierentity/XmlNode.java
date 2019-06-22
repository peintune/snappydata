package com.tomiyo.snappy.snappyclient.snappierentity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I322353 on 7/18/2016.
 */
public class XmlNode {

    public String name="";
    public String dbtype="varchar(50)";
    public String value="";
    public List<String> xpathList=new ArrayList();
    public List<String> attrList= new ArrayList();
    public List<XmlNode> subNodesList=new ArrayList<>();
    public NodeType type= NodeType.column;

    public  XmlNode(){}
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
