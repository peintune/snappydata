package com.tomiyo.snappy.snappyserver.util;

import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SnappierThreadPool
{
    private static   final ScheduledExecutorService snapppierThreadPool=Executors.newScheduledThreadPool(Configparser.getInstance().getParallelexecutornumber());

    static Logger logger = Logger.getLogger(SnappierThreadPool.class);

    private SnappierThreadPool(){
    }
    
    public static ScheduledExecutorService getThreadPool(){
        if(null==snapppierThreadPool){
            logger.error("failed to create snappier Thread pool");
        }else{

        }
        return snapppierThreadPool;
    }

}
