package com.tomiyo.snappy.snappyserver.mysql;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import com.tomiyo.snappy.snappyserver.snappierentity.SnappierXmlEntity;
import com.tomiyo.snappy.snappyserver.snappierentity.XmlNode;
import org.apache.log4j.Logger;

public class MysqlHadler
{
     Logger logger = Logger.getLogger(MysqlHadler.class);

    private  String hostname="localhost";
    private  String port="3306";
    private  String dbname=null;
    private String username="root";
    private String password=null;
    private static Connection conn=null;
    private static final MysqlHadler mysqlHandler=new MysqlHadler();
    private MysqlHadler(String hostname,String port,String dbname,String username,String password)
    {
        this.hostname=hostname;
        this.port=port;
        this.dbname=dbname;
        this.username=username;
        this.password=password;
        try {
            if(null==conn|| conn.isClosed())conn=MySqlConnection.getConnection();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    public void setHostName(String hostname){
        this.hostname=hostname;
    }
    public void setPort(String port){
        this.port=port;
    }
    public void setDbName(String dbname){
        this.dbname=dbname;
    }
    public void setUsername(String username){
        this.username=username;
    }
    public void setPassword(String password){
        this.password=password;
    }
    private MysqlHadler(){
        if(null==conn)conn=MySqlConnection.getConnection();
    }
    public Connection getConn(){
        return isValideConnection(conn);
    }
    public Connection getConn4Query(){
        return    isValideConnection(conn);
    }
    public Connection getConn4Update(){
        return isValideConnection(conn);
    }

    public static MysqlHadler getInstance(){
        return mysqlHandler;
    }
    private synchronized static Connection createConnection() throws SQLException {
        return MySqlConnection.getConnection();
    }
    private  void insertIntoMysql(String tableName, XmlNode columns, List<String> values)  {
        String columnsString="";
        columnsString=getColumnsString(columns);
        columnsString=columnsString.substring(0,columnsString.length()-1);
        String valueParas="";
        if(columnsString.split(",").length!=values.size()){
            logger.error("the count of column: "+columnsString.split(",").length+" not match ,the real column count is: "+values.size());
        }
        for(int i=0;i<columnsString.split(",").length;i++){
            valueParas+="?,";
        }
        valueParas=valueParas.substring(0,valueParas.length()-1);
        String sql="insert into "+tableName+" ("+columnsString+") values ("+valueParas+")";
        PreparedStatement pstmt=null;
        try{
            conn=isValideConnection(conn);
            pstmt =  (PreparedStatement)conn.prepareStatement(sql);
            for(int j=1;j<=values.size();j++){
                pstmt.setString(j,values.get(j-1));
            }
            int result = pstmt.executeUpdate();
            if(result!=-1){
            }else{
                logger.error(" insert sql '"+sql+"'  execute failed");
            }
        }catch(Exception e){
            try{
                conn=createConnection();
                pstmt =  (PreparedStatement)conn.prepareStatement(sql);
                for(int j=1;j<=values.size();j++){
                    pstmt.setString(j,values.get(j-1));
                }
                int result = pstmt.executeUpdate();
                if(result!=-1){
                }else{
                    logger.error(" insert sql '"+sql+"'  execute failed");
                }
            }catch (Exception error){
                logger.error(" execute '"+sql+"' failed, please check the config file and database");

            }finally {
                if(null!=pstmt)try{ pstmt.close();}catch (Exception e2){}
            }
        }finally {
            if(null!=pstmt)try{ pstmt.close();}catch (Exception e){}

        }
    }
    
    public  synchronized void syncToMysql(String tablename, XmlNode coulumns, Stack valuesStack){
        while(!valuesStack.empty()){
            List<String> values= (List<String>) valuesStack.pop();
            insertIntoMysql(tablename,coulumns,values);
        }
        valuesStack.clear();
        valuesStack.removeAllElements();
    }


    public synchronized  void executeSql(String sql){
        Statement statement=null;
        try {
            conn=isValideConnection(conn);
            statement= conn.createStatement();
            statement.executeUpdate(sql);
        }catch (Exception e) {
            try {
                conn = createConnection();
                statement= conn.createStatement();
                statement.executeUpdate(sql);
                conn.commit();
            }catch (Exception err) {
                logger.error(err.getMessage());
            }finally {
                if(null!=statement)try{ statement.close();}catch (Exception e2){}
            }
        }finally {
            if(null!=statement)try{ statement.close();}catch (Exception e){}
        }
    }

    public synchronized  void executeInsertSqls(SnappierXmlEntity snappierXmlEntity,List<String> sqls) {
        Statement statement = null;
        try {
            conn = isValideConnection(conn);
            statement = conn.createStatement();
            boolean autocommit = conn.getAutoCommit();
            conn.setAutoCommit(true);

            for (String sql : sqls) {
                try {
                    statement.executeUpdate(sql);
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate entry")) {
                        String urlLink = sql.split("\\('")[1].split("','N'")[0];
                        String deleteSql = "delete from "+snappierXmlEntity.getTableName() +" where urlLinkAddress='"+urlLink+"'";
                        logger.warn("~~~~~ delete record "+urlLink);
                        executeSql(deleteSql);
                        executeSql(sql);
                    } else {
                        logger.warn("failed to execute sql " + sql.substring(0, 50) + " , cause :" + e.getMessage());
                    }
                }
            }
            try{statement.close();}catch (Exception ey){}
            conn.commit();
            conn.setAutoCommit(autocommit);
        }catch (Exception e5){}

    }

    public synchronized  void executeSqls(List<String> sqls){
        Statement statement=null;
        try {
            conn=isValideConnection(conn);
            statement= conn.createStatement();
            boolean autocommit = conn.getAutoCommit();
            conn.setAutoCommit(true);
            for(String sql:sqls) {
                try {
                    statement.executeUpdate(sql);
                }catch (Exception e){
                    logger.warn("failed to execute sql "+sql.substring(0,50)+" , cause :"+e.getMessage());
                }
            }
            conn.commit();
            conn.setAutoCommit(autocommit);
        }catch (Exception e) {
            try {
                conn = createConnection();
                statement= conn.createStatement();
                boolean autocommit = conn.getAutoCommit();
                conn.setAutoCommit(true);

                for(String sql:sqls) {
                    try {
                        statement.executeUpdate(sql);
                    }catch (Exception e2){
                        logger.warn("failed to execute sql "+sql+" , cause :"+e2.getLocalizedMessage());
                    }
                }
                conn.commit();
                conn.setAutoCommit(autocommit);

            }catch (Exception err) {
                logger.error(err.getMessage());
            }finally {

                try {
                    conn.commit();
                } catch (SQLException e1) {
                }
                if(null!=statement)try{ statement.close();}catch (Exception e2){}
            }
        }finally {
            if(null!=statement)try{ statement.close();}catch (Exception e){}
        }
    }
    public synchronized  void executeUpdateSql(String sql){
        Statement statement=null;
        try {
            conn=isValideConnection(conn);
            statement= conn.createStatement();
            statement.executeUpdate(sql);
        }catch (Exception e){
            try{
                conn=createConnection();
                statement= conn.createStatement();
                statement.executeUpdate(sql);
            }catch (Exception error){
                logger.error("execute "+sql+" failed");
                logger.error(error.getMessage());
            }finally {
                if(null!=statement)try{ statement.close();}catch (Exception e2){}
            }
        }finally {
            if(null!=statement)try{ statement.close();}catch (Exception e){}
        }
    }

    public synchronized  void executeBathSqls(List<String> sqls){
        Statement statement=null;
        try{
            int count=1;
            conn=isValideConnection(conn);
            boolean autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            for(String sql:sqls){
                statement.addBatch(sql);
                count++;
                if(count%50==0)statement.executeBatch();
            }
            statement.executeBatch();
            conn.commit();
            conn.setAutoCommit(autocommit);
        }catch (Exception e){
            executeSqls(sqls);
        }finally {
            try {
                conn.commit();
            } catch (SQLException e) {
            }
            if(null!=statement)try{ statement.close();}catch (Exception e){}
        }
    }

    public synchronized  void executeBathSqls(String sql,List<String> values){
        PreparedStatement  statement=null;
        try{
            int count=1;
            conn=isValideConnection(conn);
            boolean autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            statement=conn.prepareStatement(sql);
            ResultSetMetaData meta = statement.getMetaData();
            ParameterMetaData parameta = statement.getParameterMetaData();
            for(int i= 0; i<values.size();i++){
                statement.setString(i,values.get(i));
                statement.addBatch();
                count++;
                if(count%50==0)statement.executeBatch();
            }
            statement.executeBatch();
            statement.clearBatch();
            conn.commit();
            conn.setAutoCommit(autocommit);
        }catch (Exception e){
            //executeSqls(sqls);
        }finally {
            if(null!=statement)try{ statement.close();}catch (Exception e){}
        }
    }

    public synchronized  ResultSet executeQuerySql(String sql){


        conn=isValideConnection(conn);

        try {
            Statement statement= conn.createStatement();
            ResultSet resultSet=statement.executeQuery(sql);
            return resultSet;
        }catch (Exception e){
            try {
                conn = createConnection();
            }catch (Exception e2){}

            try(Statement statement= conn.createStatement();){
                ResultSet resultSet=statement.executeQuery(sql);
                return resultSet;
            }catch (Exception error){
                logger.error(error.getMessage());
            }
            return null;
        }
    }

    public  void createTable(String tablename, XmlNode columns) {
        Statement stmt = null;
        try {
            conn=isValideConnection(conn);
            stmt = conn.createStatement();
            String sql = "create table if not exists " + tablename + " (id int not null auto_increment,";
            sql += getCreatetableColumns(columns);
            sql += "primary key (id))charset utf8 collate utf8_general_ci";
            int result = stmt.executeUpdate(sql);
        } catch (Exception e) {
            try {
                conn = createConnection();
                stmt = conn.createStatement();
                String sql = "create table if not exists " + tablename + " (id int not null auto_increment,";
                sql += getCreatetableColumns(columns);
                sql += "primary key (id))charset utf8 collate utf8_general_ci";
                int result = stmt.executeUpdate(sql);
            } catch (Exception error) {
                logger.error(error.getMessage());
            } finally {
                if (null != stmt) try {
                    stmt.close();
                } catch (Exception e2) {
                }

            }
        } finally {
            if (null != stmt) try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public  void createTableWithUrlAddress(SnappierXmlEntity snappierXmlEntity){
        String sql="create table if not exists "+snappierXmlEntity.getTableName()+" (id int not null auto_increment,";
        sql+=  getCreatetableColumns(snappierXmlEntity.getContentUrlListNode());
        sql+="urlLinkAddress varchar(250),isFinished char(2),primary key (id),UNIQUE KEY(urlLinkAddress))charset utf8 collate utf8_general_ci";
        executeSql(sql);
    }

    public String queryLatestRecord(String tablename,XmlNode columns){
        String sql="select * from "+tablename+" order by id desc limit 1";
        String resultString="";
        Statement stmt=null;
        ResultSet result = null;
        try{
            conn=isValideConnection(conn);
             stmt = conn.createStatement();
             result = stmt.executeQuery(sql);
          //  int i=1;
            String columnss=getColumnsString(columns);
            columnss=columnss.substring(0,columnss.length()-1);
            int columnLength=columnss.split(",").length;
            while(result.next()){
                for(int i=1;i<=columnLength;i++)
                resultString+=result.getString(i+1)+"#&#";
               // i++;
            }
        }catch(Exception e){
            try{
                conn=createConnection();
                stmt = conn.createStatement();
                 result = stmt.executeQuery(sql);
                String columnss=getColumnsString(columns);
                columnss=columnss.substring(0,columnss.length()-1);
                int columnLength=columnss.split(",").length;
                while(result.next()){
                    for(int i=1;i<=columnLength;i++)
                        resultString+=result.getString(i+1)+"#&#";
                }

            }catch (Exception error){
                logger.error(error.getMessage());
            }finally {
                if(null!=result)try{ result.close();}catch (Exception e2){}
                if(null!=stmt)try{ stmt.close();}catch (Exception e2){}
            }
        }finally {
            if(null!=result)try{ result.close();}catch (Exception e2){}
            if(null!=stmt)try{ stmt.close();}catch (Exception e2){}
        }
        return resultString;
    }


    public void insertURLS(SnappierXmlEntity snappierXmlEntity,List<String> urls){
        List<String> sqls=new ArrayList<>();
        for(String url:urls){
            String sql="insert into "+snappierXmlEntity.getTableName()+" (urlLinkAddress,isFinished) values "+"('"+url+"','N')";
            sqls.add(sql);
        }

        if(sqls.size()>0)
            executeInsertSqls(snappierXmlEntity,sqls);
    }


    public static Connection isValideConnection (Connection conn){
        Connection connection =conn;
        try {
            if(null==connection|| connection.isClosed())connection=createConnection();

            ResultSet rs =connection.createStatement().executeQuery("select 1");

            try {
                rs.close();
            }catch (Exception rt){}

        }catch (Exception e3){
            try {
                connection=createConnection();
            }catch (Exception ee){}
        }

        return  connection;

    }
    public String getColumnsString(XmlNode columnNode){
        String sqls="";

        if(columnNode.type== XmlNode.NodeType.column){
            sqls+=  columnNode.getName()+",";
        }
        if(columnNode.hasSubNodes()){
            for(XmlNode node:columnNode.getSubNodesList()){
                sqls+=getColumnsString(node);
            }
        }
        return sqls;
    }

    public String getCreatetableColumns(XmlNode columnNode){
        String sqls="";

        if(columnNode.type== XmlNode.NodeType.column){
            sqls+=  columnNode.getName()+" "+columnNode.getDbtype()+",";
        }
        if(columnNode.hasSubNodes()){
            for(XmlNode node:columnNode.getSubNodesList()){
                sqls+=getCreatetableColumns(node);
            }
        }
        return sqls;
    }
}
