package com.tomiyo.snappy.snappyclient.threadpool;

import com.tomiyo.snappy.snappyclient.message.URLMessage;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by I322353 on 9/5/2016.
 */
public class ContentParserManager {

    private static HashMap<ContentParser,Future<?>> conentParserMap=new HashMap<>();
    static  Logger logger = Logger.getLogger(ContentParserManager.class);
    static AtomicInteger activeContentParserCount=new AtomicInteger(0);
    public ContentParserManager(){
     //   reloadData();
    }

    public static int getActiveCtentParserCount(){
        return activeContentParserCount.get();
    }
    public static void decreaseCountOfContentParser(){
        activeContentParserCount.decrementAndGet();
    }

    public static void resetCountOfContentParser(){
        activeContentParserCount = new AtomicInteger(0);
    }
    public static void addOneContentParser(){
            ContentParser contentParser=new ContentParser();
            Future<?> contentParserFuture = ContentParserThreadPool.getThreadPool().submit(contentParser);
            conentParserMap.put(contentParser,contentParserFuture);
            logger.info("add one content parser");
    }

    public static ThreadPoolExecutor getTreadPool(){
        return ContentParserThreadPool.getThreadPool();
    }

    public static void addOneContentParser(URLMessage urlMessage){
        ContentParser contentParser=new ContentParser(urlMessage);
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e) {
        }
        ContentParserThreadPool.getThreadPool().submit(contentParser);
        activeContentParserCount.incrementAndGet();
      //  conentParserMap.put(contentParser,contentParserFuture);
    }
    public static  void removeOneContentParser(){
        Iterator it = conentParserMap.entrySet().iterator();
        boolean success=false;
        while(it.hasNext()){
            Map.Entry entry=(Map.Entry)it.next();
            ContentParser contentParser= (ContentParser) entry.getKey();
            if(!contentParser.isBusy()){
                Future<?>   future=(Future<?>)entry.getValue();
                future.cancel(true);
                success=true;
                logger.info("remove one content parser");
            }
        }
        if(!success){
            logger.info("all content parsers are busy");
        }
    }
}
