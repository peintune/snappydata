package com.tomiyo.snappy.snappyserver.message;

import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;

/**
 * Created by I322353 on 8/13/2016.
 */
public class URLMessage {
    private SnappierXmlEntity xmlEntity=null;
    private String URL="";


    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setXmlEntity(SnappierXmlEntity xmlEntity) {
        this.xmlEntity = xmlEntity;
    }


    public String getURL() {
        return URL;
    }

    public SnappierXmlEntity getXmlEntity() {
        return xmlEntity;
    }

}
