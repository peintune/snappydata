package com.tomiyo.snappy.snappyserver.controller;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tomiyo.snappy.snappyserver.contentParser.ContentParserManager;
import com.tomiyo.snappy.snappyserver.mysql.MysqlHadler;
import com.tomiyo.snappy.snappyserver.snappier.SnappierInitialLoader;
import com.tomiyo.snappy.snappyserver.snappier.SnappierManager;
import com.tomiyo.snappy.snappyserver.snappier.SnappierURL;
import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.util.PathManager;
import org.apache.log4j.Logger;

public class Controller implements Runnable {

    private static final Controller controller = new Controller();
    static Logger logger = Logger.getLogger(Controller.class);

    @Override
    public void run() {
//        while (true) {
//
//
//        }
        logger.info("Controller is running");
    }

    private Controller() {
    }

    public static Controller getInstance() {
        return controller;
    }

    private SnappierManager snappierManager = null;
    private ContentParserManager contentParserManager = null;

    public boolean addSnappier(String folder, String regexFileName) {
        boolean isSucess = false;
        List<String> snappierNames= getSnappiers(folder,regexFileName);
        if(null==snappierNames || snappierNames.size()==0){
            logger.info("not find the snappier "+regexFileName +"  in folder "+folder );
            return false;
        }

        for(String snappierName: snappierNames){
            String fileNameString = PathManager.getRootPath() + File.separator+"source" + File.separator+folder + File.separator + snappierName + ".xml";
            SnappierXmlEntity snappierXmlEntity = new SnappierXmlEntity(fileNameString);
            snappierXmlEntity.setFolderPath(folder);
            MysqlHadler.getInstance().executeSql("insert into snappiers (folderpath,snappiername,isactivity) values ('" + snappierXmlEntity.getFolderPath() + "'," + "'" + snappierXmlEntity.getTableName()
                    + "','N')");

            /**
             * create table
             */
            MysqlHadler.getInstance().createTableWithUrlAddress(snappierXmlEntity);


            logger.info("add one snppier "+snappierName+" success");
            if(!isSucess)
            isSucess = true;
        }
        return isSucess;
    }

    public boolean initialLoadSnappier(String folder, String regexFileName) {
        boolean isSucess = false;
        List<String> snappierNames= getSnappiers(folder,regexFileName);
        if(null==snappierNames || snappierNames.size()==0){
            logger.info("not find the snappier "+regexFileName +"  in folder "+folder );
            return false;
        }

        for(String snappierName: snappierNames){
            String fileNameString = PathManager.getRootPath() + File.separator+"source" + folder + File.separator + snappierName + ".xml";
            SnappierXmlEntity snappierXmlEntity = new SnappierXmlEntity(fileNameString);
            snappierXmlEntity.setFolderPath(folder);
            SnappierURL snappier = new SnappierURL(snappierXmlEntity);
            snappier.initialLoadPages();
            logger.info("initial load snppier "+snappierName+" success");
            if(!isSucess)
                isSucess = true;
        }
        return isSucess;
    }

    public void registerSnappierManager(SnappierManager snappierManager) {
        this.snappierManager = snappierManager;
    }

    public void registerContentParserManager(ContentParserManager contentParserManager) {
        this.contentParserManager = contentParserManager;
    }

    public void removeSnappier(String name) {
        //not support for now
    }

    public boolean stopSnappier(String folder, String regexFileName) {
        boolean isSucess = false;
        List<String> snappierNames= getSnappiers(folder,regexFileName);
        if(null==snappierNames || snappierNames.size()==0){
            logger.info("not find the snappier "+regexFileName +"  in folder "+folder );
            return false;
        }

        for(String snappierName: snappierNames){
            String fileNameString = PathManager.getRootPath() + File.separator+"source" +File.separator + folder + File.separator + snappierName + ".xml";
            SnappierXmlEntity snappierXmlEntity = new SnappierXmlEntity(fileNameString);
            snappierXmlEntity.setFolderPath(folder);
            SnappierURL snappier = new SnappierURL(snappierXmlEntity);
            MysqlHadler.getInstance().executeSql("update  snappiers set isactivity ='N' where snappiername='" + snappierXmlEntity.getTableName()
                    + "'");
            SnappierManager.removeSnappier(snappier.getName());

            logger.info("stop snppier "+snappierName+" success");
            if(!isSucess)
                isSucess = true;
        }
        return isSucess;
    }

    public void suspendSnappier() {
        //not support for now
    }



    public boolean startSnappier(String folder, String regexFileName) {
        boolean isSucess = false;
        List<String> snappierNames= getSnappiers(folder,regexFileName);
        if(null==snappierNames || snappierNames.size()==0){
            logger.info("not find the snappier "+regexFileName +"  in folder "+folder );
            return false;
        }

        for(String snappierName: snappierNames){
            String fileNameString = PathManager.getRootPath() + File.separator+"source" +File.separator + folder + File.separator + snappierName + ".xml";
            SnappierXmlEntity snappierXmlEntity = new SnappierXmlEntity(fileNameString);
            snappierXmlEntity.setFolderPath(folder);
            SnappierURL snappier = new SnappierURL(snappierXmlEntity);
            MysqlHadler.getInstance().executeSql("update  snappiers set isactivity ='Y' where snappiername='" + snappierXmlEntity.getTableName()
                    + "'");
            SnappierManager.addSnappier(snappier, snappierXmlEntity.getSleepTime() < 600 ? 600 : snappierXmlEntity.getSleepTime());
            logger.info("start one snppier "+snappierName+" success");
            if(!isSucess)
                isSucess = true;
        }
        return isSucess;
    }


    public void addOneContentParser() {
        contentParserManager.addOneContentParser();
    }

    public void removeOneContentParser() {
        contentParserManager.removeOneContentParser();
    }

    public List<String> getSnappiers(String folder, String regexFileName){
        {
            List<String> snappierResult=new ArrayList<>();
            File file = new File(PathManager.getRootPath() + File.separator+"source" + File.separator + folder);
            String fileNameString = PathManager.getRootPath() + File.separator+"source" +File.separator+ folder + File.separator + regexFileName + ".xml";
            if (new File(fileNameString).exists()) {
                snappierResult.add(regexFileName);
                return snappierResult;
            }

            File[] fileList = file.listFiles();
            if (fileList.length < 1) return null;
            Pattern pattern = Pattern.compile(regexFileName);

            for (int i = 0; i < fileList.length; i++) {
                boolean isFind = false;
                String tempFile = fileList[i].getName().replace(".xml", "");
                if (tempFile.equalsIgnoreCase(regexFileName)) {
                    isFind = true;
                } else {
                    Matcher matcher = pattern.matcher(tempFile);
                    if (matcher.find()) isFind = true;
                }
                if (isFind) {
                    snappierResult.add(tempFile);
                }

            }
            return snappierResult;
        }
    }
}
