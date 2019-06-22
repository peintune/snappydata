package com.tomiyo.snappy.snappyclient.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

public class XmlFileParser
{
    public static final SAXReader saxReader = new SAXReader();
    private Document document =null;
    private Element rootElement=null;
    public XmlFileParser(String fileName){
        document = readXmlFile(fileName);
        rootElement=document.getRootElement();
    }
    
    private  synchronized Document readXmlFile(String fileName) {
       try
        {
            synchronized (saxReader) {
                return saxReader.read(new File(fileName));
            }
        }
        catch (DocumentException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized Document getDocument(){
        return document;
    }
    
    public Element getRootElement(){
        return rootElement;
    }
    
    public Element findElement(String eleName){
        return rootElement.element(eleName);
    }
    
    public List<Element> findElements(String eleName){
        return  rootElement.elements(eleName);
    }

    public synchronized void closeDocument(String filePath){
        synchronized (saxReader) {
            saxReader.removeHandler(filePath);
        }
    }

}
