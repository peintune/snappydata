package com.tomiyo.snappy.snappyserver.util;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContentParserThreadPool
{
    private static   final ExecutorService contentParserThreadPool= Executors.newCachedThreadPool();
    static Logger logger = Logger.getLogger(ContentParserThreadPool.class);

    private ContentParserThreadPool(){

    }
    
    public static ExecutorService getThreadPool(){
        if(null==contentParserThreadPool){
            logger.error("failed to create Thread pool");
        }else{
            return  contentParserThreadPool;
        }
        return contentParserThreadPool;
    }

}
