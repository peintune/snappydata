package com.tomiyo.snappy.snappyserver.test;

import com.tomiyo.snappy.snappyserver.httpUtil.HtmlunitClient;

/**
 * Created by I322353 on 10/5/2016.
 */
public class HttpTest {

    public static  void main(String[] args){

        HtmlunitClient htmlunitClient=new HtmlunitClient(true,null,0,true);

        String url="http://aba.anjuke.com/sale/o5-p1/#filtersort/";

        //url="http://shanghai.anjuke.com/sale/";
        url="http://aba.anjuke.com/prop/view/H265187863?from=structured_dict&spread=filtersearch_p&position=5&now_time=1475682962";
        url="http://ab.anjuke.com/sale/o5-p1/#filtersort/";
        //url="http://cn.bing.com";

        String reuslt="";

        try {
            reuslt=htmlunitClient.doGetOneTime(url);
            System.out.println(reuslt);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
