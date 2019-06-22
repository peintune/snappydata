package com.tomiyo.snappy.snappyserver.util;

import com.tomiyo.snappy.snappyserver.web.MessageController;
import org.apache.log4j.Logger;

import java.io.File;

public class PathManager
{
    private  String rootPath=null;
    private static final PathManager pathManager=new PathManager();
    org.apache.log4j.Logger logger = Logger.getLogger(MessageController.class);

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
                    path=path.replace("snappyserver"+"/"+"target"+"/"+"classes"+"/","");
                    path= path+ File.separator+ "output"+File.separator+"snappyserver";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.info("程序路徑："+ path);

        rootPath=path;
//        try
//        {
//          rootPath = directory.getCanonicalPath();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
    
    }
}
