package com.tomiyo.snappy.snappyclient.main;

import com.tomiyo.snappy.snappyclient.httpUtil.HtmlunitClient;

/**
 * Created by dell on 2017/4/18.
 */
public class IPmaintest {

    public static void main(String[] args){
        HtmlunitClient htmlunit= new HtmlunitClient(false,"",80,true);

        int i=1;
        //651806
        while (true) {

            String ps="";
            String il=i+"";
            int lengh= il.length();
            if(lengh<6){
                for(int j=0;j<6-lengh;j++){
                    il="0"+il;
                }
            }
            if(i%3000==0)
            System.out.println(il);
        String url="http://www.66ip.cn/getzh.php?getzh=2017041"+il+"&getnum=2&isp=0&anonymoustype=4&start=&ports=8080&export=&ipaddress=&area=0&proxytype=2&api=https";
            try {
                String result = htmlunit.doGet(url);
              // String result= htmlunit.doGet("https://www.baidu.com");
                if(!result.contains("订单不存在") &&result!="") {
                    System.out.println("find    "+url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }
}
