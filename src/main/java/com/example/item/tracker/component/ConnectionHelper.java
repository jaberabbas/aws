
package com.example.item.tracker.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Component
public class ConnectionHelper {

    public Connection getConnection(String host, String user, String password) throws SQLException {
        Connection connection = null;
        try {
            String url2 = "jdbc:postgresql://" + host + ":5432/postgres";
            connection = DriverManager.getConnection(url2, user, password);
        } catch (SQLException e) {
            log.error("Failed to connect to the database. Error: {}", e.getMessage());
        }
        return connection;
    }

    public void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }

        } catch (SQLException e) {
            log.error("Failed to close database connection. Error: {}", e.getMessage());

        }
    }
}