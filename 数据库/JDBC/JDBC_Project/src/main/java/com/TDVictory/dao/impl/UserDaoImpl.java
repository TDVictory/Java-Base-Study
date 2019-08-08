package com.TDVictory.dao.impl;

import com.TDVictory.Util.JDBCUtil;
import com.TDVictory.dao.UserDao;

import java.sql.*;

public class UserDaoImpl implements UserDao {
    @Override
    public void query() {
        Connection connection = null;
        Statement st = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtil.getConnection();
            st = connection.createStatement();
            ps = connection.prepareStatement("select * from user where username=?");
            ps.setString(1,"wangwu");

            rs = ps.executeQuery();
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

    @Override
    public void logIn(String username, String password) {
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        String sql = "select * from user where username ='" + username + "' and password ='" + password + "'";

        try {
            connection = JDBCUtil.getConnection();
            PreparedStatement ps = connection.prepareStatement("select * from user where username =? and password=?");
            //st = connection.createStatement();
            //rs = st.executeQuery(sql);
            ps.setString(1,username);
            ps.setString(2,password);
            rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("登陆成功");
            }
            else{
                System.out.println("登陆失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.release(connection,st,rs);
        }
    }
}
