package com.tomiyo.snappy.snappyserver.contentParser;

import com.tomiyo.snappy.snappyserver.util.ContentParserThreadPool;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by I322353 on 9/5/2016.
 */
public class ContentParserManager {

    private static HashMap<ContentParser,Future<?>> conentParserMap=new HashMap<>();
    static Logger logger = Logger.getLogger(ContentParserManager.class);
    public ContentParserManager(){
     //   reloadData();
    }


    public static void addOneContentParser(){
            ContentParser contentParser=new ContentParser();
            Future<?> contentParserFuture = ContentParserThreadPool.getThreadPool().submit(contentParser);
            conentParserMap.put(contentParser,contentParserFuture);
             logger.info("add one content parser");
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
                logger.info("remove  one content parser");
            }
        }
        if(!success){
            logger.info("all content parser are busy ");
        }
    }
}
