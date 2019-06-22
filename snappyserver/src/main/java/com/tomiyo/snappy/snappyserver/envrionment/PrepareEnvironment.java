package com.tomiyo.snappy.snappyserver.envrionment;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.tomiyo.snappy.snappyserver.util.Configparser;
import com.tomiyo.snappy.snappyserver.mysql.MySqlConnection;

/**
 * Created by tomiyo on 8/13/2016.
 */
public class PrepareEnvironment {

    Connection conn=null;

    public void initialEnvironment(){
         conn=getConnection();
        createSnappierTable();
        UrlResultCheckThread urlResultCheckThread=new UrlResultCheckThread();
        Thread urlCheck=new Thread(urlResultCheckThread,"URL result Check");
        urlCheck.setDaemon(true);
        urlCheck.start();
    }

    public Connection getConnection(){
        return MySqlConnection.getConnection();
    }

    public void createSnappierTable(){
        try {
        Statement stmt = conn.createStatement();
        String sql="create table if not exists "+"snappiers"+" (id int not null auto_increment ,folderpath varchar(100),snappiername varchar(100),isactivity char(2),first_starttime datetime, ";
        sql+="primary key (id))charset utf8 collate utf8_general_ci";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(null!=conn)
                conn.close();
            } catch (SQLException e) {
                if(Configparser.getInstance().getDebugLog())e.printStackTrace();
            }
        }
    }
}
