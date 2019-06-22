package com.tomiyo.snappy.snappyserver.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlFileParser
{
    public static final SAXReader saxReader = new SAXReader();
    private Document document =null;
    private Element rootElement=null;
    public  XmlFileParser(String fileName){
        document = readXmlFile(fileName);
        rootElement=document.getRootElement();
    }
    
    private  synchronized Document readXmlFile(String fileName) {
       try
        {
            synchronized (saxReader) {
                FileInputStream ifile = new FileInputStream(fileName);
                InputStreamReader ir = new InputStreamReader(ifile, "UTF-8");

                Document document = saxReader.read(ir);
                document.setXMLEncoding("UTF-8");
                return document;
            }
        }
        catch (Exception e)
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
