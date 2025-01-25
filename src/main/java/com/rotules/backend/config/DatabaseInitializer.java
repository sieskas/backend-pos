package com.rotules.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.*;

@Configuration
public class DatabaseInitializer {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void initialize() {
        // Extrait le nom de la base de données de l'URL
        String dbName = extractDatabaseName(dbUrl);
        // Crée l'URL pour se connecter à MySQL sans spécifier de base de données
        String baseUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/"));

        try (Connection connection = DriverManager.getConnection(baseUrl, dbUsername, dbPassword)) {
            try (Statement statement = connection.createStatement()) {
                // Vérifie si la base de données existe
                ResultSet resultSet = connection.getMetaData().getCatalogs();
                boolean dbExists = false;

                while (resultSet.next()) {
                    String databaseName = resultSet.getString(1);
                    if (databaseName.equals(dbName)) {
                        dbExists = true;
                        break;
                    }
                }

                if (!dbExists) {
                    // Crée la base de données si elle n'existe pas
                    String sql = "CREATE DATABASE IF NOT EXISTS " + dbName;
                    statement.executeUpdate(sql);
                    System.out.println("Database " + dbName + " created successfully");
                } else {
                    System.out.println("Database " + dbName + " already exists");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private String extractDatabaseName(String url) {
        int lastSlash = url.lastIndexOf("/");
        int questionMark = url.indexOf("?");
        if (questionMark == -1) {
            return url.substring(lastSlash + 1);
        }
        return url.substring(lastSlash + 1, questionMark);
    }
}