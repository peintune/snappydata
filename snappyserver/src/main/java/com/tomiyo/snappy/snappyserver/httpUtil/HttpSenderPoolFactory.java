package com.tomiyo.snappy.snappyserver.httpUtil;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
/**
 * Created by hekun on 8/21/2016.
 */
public class HttpSenderPoolFactory extends   BaseKeyedPooledObjectFactory<Boolean,HtmlunitClient>{



    @Override
    public void passivateObject(Boolean key, PooledObject<HtmlunitClient> p) throws Exception {
        HtmlunitClient htmlunitClient = (HtmlunitClient)p;
        htmlunitClient.reset();
    }

    @Override
    public void destroyObject(Boolean key, PooledObject<HtmlunitClient> p) throws Exception {
        p=null;
    }

    @Override
    public HtmlunitClient create(Boolean isJsEnable) throws Exception {
        HtmlunitClient htmlunitClient = new HtmlunitClient((boolean)isJsEnable);
        return htmlunitClient;
    }

    @Override
    public PooledObject<HtmlunitClient> wrap(HtmlunitClient htmlunitClient) {
        return new DefaultPooledObject<HtmlunitClient>(htmlunitClient);
    }
}
