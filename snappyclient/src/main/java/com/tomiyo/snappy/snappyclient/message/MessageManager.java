package com.tomiyo.snappy.snappyclient.message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageManager
{
    private static final BlockingQueue<IMessage> meesageQueue= new LinkedBlockingQueue<IMessage>();
    
    private MessageManager(){}
    
    public static void addOneMessage(IMessage message){
        meesageQueue.add(message);
    }
    
    public static IMessage fetchOneMessage(){
        try
        {
            if(!meesageQueue.isEmpty()) {
                return meesageQueue.take();
            }else{
            return null;
        }
        }
        catch (InterruptedException e)
        {
            return null;
        }
    }
    public static int getQueueNumber(){
        return meesageQueue.size();
    }

    public static boolean hasMessage(){
        return !meesageQueue.isEmpty();
    }

}
