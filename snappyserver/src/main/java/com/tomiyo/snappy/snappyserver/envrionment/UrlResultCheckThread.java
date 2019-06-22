package com.tomiyo.snappy.snappyserver.envrionment;

import com.tomiyo.snappy.snappyserver.mysql.MySqlConnection;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.message.URLQueueManager;
import com.tomiyo.snappy.snappyserver.util.PathManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Stack;

/**
 * Created by I322353 on 9/16/2016.
 */
public class UrlResultCheckThread implements  Runnable{
     Logger logger = Logger.getLogger(UrlResultCheckThread.class);

    @Override
    public void run() {

        while(true) {
            if (URLQueueManager.currentQueueSize()>10) {
                try {
                    Thread.currentThread().sleep(30000);
                } catch (InterruptedException e) {
                    logger.error(e.getCause());
                }
            } else {
                boolean isFinished = reloadData();
                if (isFinished) {
                    try {
                        Thread.currentThread().sleep(60000);
                    } catch (InterruptedException e) {
                        logger.error(e.getCause());
                    }
                }
            }
        }
    }

    private boolean reloadData(){

       Connection conn4Query = MysqlHadler.getInstance().getConn();
       // conn4Query= MysqlHadler.isValideConnection(conn4Query);
        Connection conn = MySqlConnection.getConnection();
        try ( Statement statement=conn4Query.createStatement();
              ResultSet resultset= statement.executeQuery("select folderpath,snappiername from snappiers where isactivity='Y'" )){
            //int i=0;
            int totalCount=0;
            Statement statement2 = conn.createStatement();
            while(resultset.next()) {
                int count=0;
                String folder=resultset.getString("folderpath");
                String tablename=resultset.getString("snappiername");
                ResultSet resultset2 = statement2.executeQuery("select urlLinkAddress from "+tablename+" where isFinished='N' limit 300");
                SnappierXmlEntity snappierXmlEntity=null;
                Stack stack=null;
                boolean isMark=false;

                while(resultset2.next()){
                    if(!isMark){
                        snappierXmlEntity=  generateXMLEntity(folder,tablename);
                        stack=new Stack();
                        isMark=true;
                    }
                    String url=  resultset2.getString("urlLinkAddress");
                    count++;
                    totalCount++;
                    if(totalCount>300)break;
                    stack.add(url);
                }
                if(null!=snappierXmlEntity&&null!=stack&&stack.size()>0){
                    logger.info("reload "+count+" record from "+ snappierXmlEntity.getTableName());
                    URLQueueManager.addBacthURLOnly2Queue(snappierXmlEntity,stack);
                }

                if(null!= resultset2){
                    resultset2.close();
                }
            }
            if(null!= statement2){
                statement2.close();
            }
        } catch (Exception e) {
                  e.printStackTrace();
                  logger.error(e);
        }
        return true;
    }


    public SnappierXmlEntity generateXMLEntity(String folderPath,String tableName){
        String fileNameString= PathManager.getRootPath()+ File.separator+"source" +File.separator+folderPath+File.separator+tableName+".xml";
        SnappierXmlEntity snappierXmlEntity=new SnappierXmlEntity(fileNameString);
        return snappierXmlEntity;
    }
}

