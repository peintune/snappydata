package com.tomiyo.snappy.snappyclient.message;

import com.tomiyo.snappy.snappyclient.snappierentity.XmlNode;

import java.util.Stack;

public class Message implements IMessage
{
    private String tablename="";
    private XmlNode columns=null;
    private Stack values=null;
    
    public String getTablename()
    {
        return tablename;
    }
    public void setTablename(String tablename)
    {
        this.tablename = tablename;
    }
    public XmlNode getColumns()
    {
        return columns;
    }
    public void setColumns(XmlNode columns)
    {
        this.columns = columns;
    }
    public Stack getValues()
    {
        return values;
    }
    public void setValues(Stack values)
    {
        this.values = values;
    }
    
    
}
