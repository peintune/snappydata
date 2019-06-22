package com.tomiyo.snappy.snappyclient.util;

import org.apache.log4j.Logger;

import java.io.File;

public class PathManager
{
    private  String rootPath=null;
    private static final PathManager pathManager=new PathManager();
    org.apache.log4j.Logger logger = Logger.getLogger(PathManager.class);

    private PathManager(){
        setRootPath();
    }
    public static String getRootPath(){

          return pathManager.rootPath;
    }
    
    private  void setRootPath(){
        File directory = new File("");
        String path=directory.getAbsolutePath();

        if(path.contains("output")){
            rootPath=path;
        }else {
            try {
                path = getClass().getResource("/").toURI().getPath();
                if(path.contains("classes")){
                    path=path.replace("snappyclient"+"/"+"target"+"/"+"classes"+"/","");
                    path= path+ File.separator+ "output"+File.separator+"snappyclient";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.info("程序路徑："+ path);

        rootPath=path;


//        File directory = new File("");// tomcat运行path ,需要重新编译
//        String path="";
//        try {
//            path = getClass().getResource("/").toURI().getPath();
//            System.out.println(path);
//            path=path.replace("WEB-INF"+"/"+"classes"+"/","");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        rootPath=path;

    }
}
