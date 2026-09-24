package com.bank_of_cli.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.bank_of_cli.util.AppLogger;

public class ConnectionFactory {
    public static final ConnectionFactory connectionFactory = new ConnectionFactory();
    private Properties props = new Properties();

    private ConnectionFactory() {
        try (InputStream propertiesStream = ConnectionFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (propertiesStream == null) {
                throw new IllegalStateException("Database configuration file was not found.");
            }
            props.load(propertiesStream);
        } catch (IOException | IllegalStateException e) {
            AppLogger.error("Could not load database configuration", e);
            throw new IllegalStateException("Could not load database configuration", e);
        }
    }

    public static ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                props.getProperty("DB_URL"),
                props.getProperty("DB_USER"),
                props.getProperty("DB_PASSWORD"));
        } catch (SQLException e) {
            AppLogger.error("Could not connect to the database", e);
            throw new IllegalStateException("Could not connect to the database", e);
        }
    }
}
