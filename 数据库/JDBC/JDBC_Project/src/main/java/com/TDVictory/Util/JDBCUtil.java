package com.TDVictory.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtil {

    static String url = null;
    static String user = null;
    static String password = null;
    static {
        Properties properties = new Properties();
        try {
            InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
            //导入输入流
            properties.load(is);

            //读取属性
            url = properties.getProperty("url");
            user = properties.getProperty("user");
            password = properties.getProperty("password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     * @param cn
     * @param st
     * @param rs
     */
    public static void release(Connection cn, Statement st, ResultSet rs){
        closeResultSet(rs);
        closeStatement(st);
        closeConnection(cn);
    }

    public static Connection getConnection(){
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //1.注册驱动
            //DriverManager.registerDriver(new com.mysql.jdbc.Driver());

            connection = DriverManager.getConnection(url,user,password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
       return connection;

    }

    private static void closeResultSet(ResultSet rs){
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                rs = null;
            }
        }
    }

    private static void closeStatement(Statement st){
        if(st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                st = null;
            }
        }
    }

    private static void closeConnection(Connection cn){
        if(cn != null) {
            try {
                cn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cn = null;
            }
        }
    }

}
