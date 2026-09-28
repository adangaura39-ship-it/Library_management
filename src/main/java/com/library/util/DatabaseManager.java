package com.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Central place for obtaining JDBC connections and running startup schema
 * setup. Reads the DB URL from config.properties (falling back to a
 * LIBRARY_DB_URL environment variable) so nothing is hardcoded in source.
 */
public final class DatabaseManager {

    private static final String CONFIG_FILE = "config.properties";
    private static String dbUrl;

    private DatabaseManager() {
    }

    private static synchronized String resolveDbUrl() {
        if (dbUrl != null) {
            return dbUrl;
        }
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read " + CONFIG_FILE + ": " + e.getMessage());
        }

        String url = props.getProperty("db.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("LIBRARY_DB_URL");
        }
        if (url == null || url.isBlank()) {
            url = "jdbc:sqlite:library.db"; // safe local default, no credentials involved
        }
        dbUrl = url;
        return dbUrl;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(resolveDbUrl());
        // SQLite doesn't enforce FK constraints unless explicitly told to.
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /**
     * Runs schema.sql (from the project root or classpath) against the
     * database if the tables don't already exist. Safe to call every startup.
     */
    public static void initializeSchema() {
        String schemaSql = loadSchemaText();
        if (schemaSql == null) {
            System.err.println("Warning: schema.sql not found; assuming tables already exist.");
            return;
        }
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            for (String statement : schemaSql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize schema: " + e.getMessage());
        }
    }

    private static String loadSchemaText() {
        // Try the project root first (useful when running from source tree).
        Path rootPath = Path.of("schema.sql");
        if (Files.exists(rootPath)) {
            try {
                return Files.readString(rootPath);
            } catch (IOException ignored) {
                // fall through to classpath attempt
            }
        }
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) return null;
            return new String(in.readAllBytes());
        } catch (IOException e) {
            return null;
        }
    }
}
