package com.tomiyo.snappy.snappyserver.mysql;

import com.tomiyo.snappy.snappyserver.util.Configparser;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by I322353 on 8/13/2016.
 */
public class MySqlConnection {
    static Logger logger = Logger.getLogger(MySqlConnection.class);

    public static Connection getConnection()  {
        Configparser configparser = Configparser.getInstance();
        String hostname = configparser.getHostname();
        String portnumber = configparser.getPortnumber();
        String dbname = configparser.getDbname();
        String username = configparser.getUsername();
        String password = configparser.getPassword();
        Connection conn = null;
        String url = "jdbc:mysql://" + hostname + ":" + portnumber + "/" + dbname + "?"
                + "user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(true);
            return conn;
        } catch (Exception e) {
            logger.error("connect to database failed,please check the config file or the database");
            logger.error(e.getMessage());
        }
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {

        }
        return conn;
    }


}
