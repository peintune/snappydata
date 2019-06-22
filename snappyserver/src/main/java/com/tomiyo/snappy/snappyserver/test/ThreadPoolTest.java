package com.tomiyo.snappy.snappyserver.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.tomiyo.snappy.snappyserver.snappier.Snappier;

public class ThreadPoolTest
{

    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        ScheduledExecutorService executor=Executors.newScheduledThreadPool(1);
        Snappier snappier=new Snappier("1111");
        Snappier snappier2=new Snappier("22222");
        Snappier snappier3=new Snappier("3333");
        executor.scheduleAtFixedRate(snappier, 0,3, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(snappier2,0, 3, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(snappier3,0, 3, TimeUnit.SECONDS);

    }

}
