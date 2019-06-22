package com.tomiyo.snappy.snappyserver.message;

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
            return meesageQueue.take();
        }
        catch (InterruptedException e)
        {
            return null;
        }
    }

    public static int getMessageCount(){
        return meesageQueue.size();
    }
    public static boolean hasMessage(){
        return meesageQueue.size()>0;
    }
}
