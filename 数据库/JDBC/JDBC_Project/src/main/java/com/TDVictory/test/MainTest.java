package com.TDVictory.test;

import com.mysql.jdbc.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainTest {
    public static void main(String[] args) {


        try {
            //1.注册驱动
            DriverManager.registerDriver(new Driver());

            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/day02","root","vivedu");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
