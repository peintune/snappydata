package com.tomiyo.snappy.snappyserver.snappier;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.util.SnappierThreadPool;

public class SnappierManager
{

    private static HashMap<String,ScheduledFuture<?>> snappiersMap=new HashMap<>();
    
    private static final SnappierManager snappierManager =new SnappierManager();
    private SnappierManager(){
        
    }
    
    public static void addSnappier(Snappier snappier,int seconds){
        if(snappiersMap.get(snappier.name)!=null) {
                //no need add
        }else{
            String tableName=snappier.name;
            String sql="select first_starttime from snappiers where snappiername='"+tableName+"'";


            long  starttime=new Date().getTime();
            try ( ResultSet rs= MysqlHadler.getInstance().executeQuerySql(sql)){
                while (rs.next()){
                    starttime = rs.getTimestamp("first_starttime").getTime();
                }
            } catch (Exception e) {
            }
          long delpaytime= starttime- new Date().getTime();
            int  initDelayTime=0;
            if(delpaytime<0){
                initDelayTime=0;
            }else{
                initDelayTime= (int) (delpaytime/1000);
            }
            ScheduledFuture<?> snappierFuture = SnappierThreadPool.getThreadPool().scheduleWithFixedDelay(snappier,initDelayTime, seconds, TimeUnit.SECONDS);
            snappiersMap.put(snappier.name, snappierFuture);
        }
    }
    
    public static void removeSnappier(String name){
        try{
            snappiersMap.get(name).cancel(true);
        }catch (Exception e){

        }
    }
    public static void removeSnappierAfterDone(String name){
        snappiersMap.get(name).cancel(false);
    }
}
