// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.item_tracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHelper {

    public static Connection getConnection(String host, String user, String password) throws SQLException {
        Connection connection = null;
        try {
            String url2 = "jdbc:postgresql://" + host + ":5432/postgres";
            System.out.println("url2: " + url2);
            connection =  DriverManager.getConnection(url2, user, password);
            System.out.println("connection was successful: " + connection);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Error: " + e.getMessage());
        }
        return connection;
    }

    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}