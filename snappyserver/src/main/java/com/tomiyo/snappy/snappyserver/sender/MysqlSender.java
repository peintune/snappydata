package com.tomiyo.snappy.snappyserver.sender;

import com.tomiyo.snappy.snappyserver.snappierentity.XmlNode;
import com.tomiyo.snappy.snappyserver.message.ContentMessage;
import com.tomiyo.snappy.snappyserver.message.MessageManager;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MysqlSender implements Runnable,ISender
{
    Logger logger = Logger.getLogger(MysqlSender.class);
    MysqlHadler mysqlHadler=MysqlHadler.getInstance();

    @Override
    public void run()
    {
        List<String> batchSqls= new ArrayList<>();
        while (true)
        {
            if(batchSqls.size()<50&&MessageManager.hasMessage()){
                ContentMessage message= (ContentMessage)MessageManager.fetchOneMessage();//从消息管理者那里消费一个消息
                SnappierXmlEntity snappierXmlEntity=message.getSnappierXmlEntity();

                if(!message.isValide()){
                    String sql = "update " + snappierXmlEntity.getTableName()+" set isFinished='D'"+" where urlLinkAddress='" + message.getUrlLinkAddress() + "'";
                    batchSqls.add(sql);
                    continue;
                }
                List<String> columnResult=message.getColumnResult();
                String urlLinkAddress=message.getUrlLinkAddress();

                StringBuffer columnBuffer = new StringBuffer();
                getColumnsString(snappierXmlEntity.getContentUrlListNode(),columnBuffer);
                String columnString=columnBuffer.toString();

                StringBuffer columnDBTypeBuffer = new StringBuffer();
                getColumnsDBType(snappierXmlEntity.getContentUrlListNode(),columnDBTypeBuffer);
                String columnDBTypes =columnDBTypeBuffer.toString();

                columnString=columnString.substring(0,columnString.length()-1);
                columnDBTypes=columnDBTypes.substring(0,columnDBTypes.length()-1);

                String[] columnStringArray=columnString.split(",");
                String[] columnDBTypeArray=columnDBTypes.split(",");

                boolean isNullValid = false;
                if(columnStringArray.length!=columnResult.size()){
                   // logger.error("the count of columns not match,should be:"+columnStringArray.length+",but is:"+columnResult.size());
                    isNullValid = true;

                   // continue;
                }
                StringBuilder sb = new StringBuilder();
                int nullColumn=0;
                for(int i=0;i<columnStringArray.length;i++){
                    String columnValue = columnResult.get(i);
                    if(null==columnValue|| columnValue.equals("")){
                        nullColumn++;
                    }
                    sb.append(columnStringArray[i]).append("='").append(columnValue).append("',");
                }

                    if(columnResult.size()<=6 && nullColumn>columnResult.size()/2){
                        //logger.error(" too much null columns, not save the data,  the table is "+  snappierXmlEntity.getTableName()+" , the url is "+urlLinkAddress);
                        isNullValid=true;
                    }else if(columnResult.size()<=15&& (nullColumn> (columnResult.size()-columnResult.size() /3))){
                       // logger.error(" too much null columns, not save the data,  the table is "+  snappierXmlEntity.getTableName()+" , the url is "+urlLinkAddress);
                        isNullValid = true;
                     }
                    else if ( nullColumn > (columnResult.size()-columnResult.size() /6)){
                       // logger.error(" too much null columns, not save the data,  the table is "+  snappierXmlEntity.getTableName()+" , the url is "+urlLinkAddress);
                        isNullValid=true;
                    }

                    String sql ="";
                    if (isNullValid){
                         sql = "update " + snappierXmlEntity.getTableName()+" set isFinished='E'"+" where urlLinkAddress='" + urlLinkAddress + "'";
                    }else{
//                        String sqlIsFinished = "select isFinished from "+snappierXmlEntity.getTableName()+" where urlLinkAddress='"+ urlLinkAddress+"'";
//
//                        boolean isFinished = false;
//
//                        try ( ResultSet rs = mysqlHadler.executeQuerySql(sqlIsFinished);){
//                            if (rs.next()) {
//                                String isfinished = rs.getString("isFinished");
//                                if(isfinished.equalsIgnoreCase("Y")){
//                                    isFinished = true;
//                                }
//                            }
//                        }catch (Exception ee){
//
//                        }

//                        if(isFinished){
//                            String sqlDelete = "delete from " + snappierXmlEntity.getTableName()+" where urlLinkAddress='"+ urlLinkAddress+"'";
//                            batchSqls.add(sqlDelete);
//                            String sqlInsert = "insert into "+ snappierXmlEntity.getTableName()+" (urlLinkAddress,isFinished) values ('"+urlLinkAddress+"','Y')";
//                            batchSqls.add(sqlInsert);
//                        }
                            sb.append("isFinished='Y'").append(" where urlLinkAddress='").append(urlLinkAddress).append("'").insert(0," set ").insert(0,snappierXmlEntity.getTableName()).insert(0,"update ");
                     //   sql = "update " + snappierXmlEntity.getTableName() + " set " + sb.toString() + " where urlLinkAddress='" + urlLinkAddress + "'";
                        sql= sb.toString();
                    }

                   batchSqls.add(sql);

            }else{
                try {
                    if(batchSqls.size()>0) {
                        mysqlHadler.executeBathSqls(batchSqls);
                        logger.info("delete or update " + batchSqls.size() + " records");
                        batchSqls.clear();

                        if(batchSqls.size()>=50){
                            continue;
                        }else{
                            Thread.currentThread().sleep(20 * 1000);
                            continue;
                        }
                    }else {
                        Thread.currentThread().sleep(20 * 1000);
                        continue;

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getColumnsString(XmlNode columnNode,StringBuffer sb){
        String sqls="";

        if(columnNode.type== XmlNode.NodeType.column){
            sb.append(columnNode.getName()).append(",");
        }
        if(columnNode.hasSubNodes()){
            for(XmlNode node:columnNode.getSubNodesList()){
                getColumnsString(node,sb);
            }
        }
    }


    public void getColumnsDBType(XmlNode columnNode,StringBuffer sb){
        String sqls="";
        if(columnNode.type== XmlNode.NodeType.column){
            sb.append(columnNode.getDbtype()).append(",");
        }
        if(columnNode.hasSubNodes()){
            for(XmlNode node:columnNode.getSubNodesList()){
                getColumnsDBType(node,sb);
            }
        }
    }
}
