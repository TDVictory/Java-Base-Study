package com.TDVictory.test;

import com.TDVictory.Util.JDBCUtil;
import com.TDVictory.dao.UserDao;
import com.TDVictory.dao.impl.UserDaoImpl;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainTestDemo {
    @Test
    public void testQuery(){
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        String sql = "select * from product";

        try {
            connection = JDBCUtil.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery("select * from user");
            while (rs.next()){
                int id = rs.getInt("uid");
                String str = rs.getString("username");

                System.out.println(id + " " + str);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.release(connection,st,rs);
        }
    }

    @Test
    public void testInsert(){
        Connection connection = null;
        Statement statement = null;

        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();
            int result = statement.executeUpdate("INSERT INTO USER VALUES (null,'zhaoliu',345,13744448888)");
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDelete(){
        Connection connection = null;
        Statement statement = null;

        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();
            int result = statement.executeUpdate("DELETE from user where uid = 4");
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdate(){
        Connection connection = null;
        Statement statement = null;

        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();
            int result = statement.executeUpdate("UPDATE user set username = 'wangwu' where uid = 1");
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindAll(){
        UserDao dao = new UserDaoImpl();
        dao.query();
    }

    @Test
    public void testLogin(){
        UserDao userDao = new UserDaoImpl();
        userDao.logIn("wangwu","22343' or '1=1");
    }
}
