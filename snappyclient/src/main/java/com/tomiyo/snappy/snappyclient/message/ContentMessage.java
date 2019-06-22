package com.tomiyo.snappy.snappyclient.message;

import com.tomiyo.snappy.snappyclient.snappierentity.SnappierXmlEntity;

import java.util.List;

/**
 * Created by I322353 on 9/5/2016.
 */
public class ContentMessage implements  IMessage{
    private SnappierXmlEntity snappierXmlEntity=null;
    private List<String> columnResult=null;
    private String urlLinkAddress="";
    private  boolean isValide=true;

    public boolean isValide() {
        return isValide;
    }

    public void setValide(boolean valide) {
        isValide = valide;
    }


    public String getUrlLinkAddress() {
        return urlLinkAddress;
    }

    public void setUrlLinkAddress(String  urlLinkAddress) {
        this.urlLinkAddress = urlLinkAddress;
    }

    public SnappierXmlEntity getSnappierXmlEntity() {
        return snappierXmlEntity;
    }

    public void setSnappierXmlEntity(SnappierXmlEntity snappierXmlEntity) {
        this.snappierXmlEntity = snappierXmlEntity;
    }

    public List<String> getColumnResult() {
        return columnResult;
    }

    public void setColumnResult(List<String> columnResult) {
        this.columnResult = columnResult;
    }

}
