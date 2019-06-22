package com.tomiyo.snappy.snappyserver.message;

import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.mysql.MySqlConnection;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Stack;

/**
 * Created by I322353 on 8/13/2016.
 */
public class URLQueueManager {

    static Logger logger = Logger.getLogger(URLQueueManager.class);

    private static final BlockingQueue<URLMessage> meesageQueue= new LinkedBlockingQueue<URLMessage>();
    private static Connection conn= MySqlConnection.getConnection();

    public synchronized static void addOne(URLMessage urlMessage){

        try {
            meesageQueue.put(urlMessage);
        } catch (InterruptedException e) {
            logger.error(e.getCause());
        }
    }

    public static URLMessage fetchOne(){
        try {
            if(meesageQueue.isEmpty()){
                return null;
            }
            return meesageQueue.take();
        } catch (InterruptedException e) {
            logger.error(e.getCause());
            return null;
        }
    }
    public static boolean hasMessage(){
        return !meesageQueue.isEmpty();
    }

    public static int currentQueueSize(){
        return meesageQueue.size();
    }
    public synchronized static void  addBacthURLMessage(SnappierXmlEntity entity, Stack<String> stackURLs){
        List<String> urls= new ArrayList<>();

        logger.info(entity.getTableName()+" add  "+stackURLs.size() +" records into database and message queue");
        while(stackURLs.size()>0){
            String url=stackURLs.pop();
            urls.add(url);
//            URLMessage urlMessage=new URLMessage();
//            urlMessage.setXmlEntity(entity);
//            urlMessage.setURL(url);
           // meesageQueue.add(urlMessage);
        }
        String checkSql = "select urlLinkAddress from "+ entity.getTableName() + " where id>=(select id from "+entity.getTableName()+" where urlLinkAddress = '"+urls.get(0)+"')";


        try(ResultSet rs =MysqlHadler.getInstance().executeQuerySql(checkSql)) {
            while (rs.next()) {
                if (rs.getString("urlLinkAddress").equalsIgnoreCase(urls.get(0))) {
                    urls.remove(0);
                } else {
                    break;
                }
                if (urls.size() == 0) break;
            }
        }catch (Exception el){

        }


        MysqlHadler.getInstance().insertURLS(entity,urls);
        stackURLs.clear();
        stackURLs.removeAllElements();
    }
    public synchronized static void  addBacthURLOnly2Queue(SnappierXmlEntity entity,Stack<String> stackURLs){
        logger.info(entity.getTableName()+" add  "+stackURLs.size() +" records into database and message queue");
        while(stackURLs.size()>0){
            String url=stackURLs.pop();
            URLMessage urlMessage=new URLMessage();
            urlMessage.setXmlEntity(entity);
            urlMessage.setURL(url);
            meesageQueue.add(urlMessage);
        }
        entity = null;
        stackURLs.clear();
        stackURLs.removeAllElements();
    }
}
