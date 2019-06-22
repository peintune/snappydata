package com.tomiyo.snappy.snappyclient.threadpool;

import com.tomiyo.snappy.snappyclient.util.Configparser;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ContentParserThreadPool
{
    private static   final ThreadPoolExecutor contentParserThreadPool= (ThreadPoolExecutor)Executors.newFixedThreadPool(Configparser.getInstance().getContentparsernumber());
    static Logger logger = Logger.getLogger(ContentParserThreadPool.class);
    private ContentParserThreadPool(){

    }
    
    public static ThreadPoolExecutor getThreadPool(){
        if(null==contentParserThreadPool){
            logger.error("Thread pool creating error");
        }else{
            return  contentParserThreadPool;
        }
        return contentParserThreadPool;
    }

}
