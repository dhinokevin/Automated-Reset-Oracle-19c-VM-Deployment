package com.example.OracleReset.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseValidator {

    public static void validateDatabase(String url, String user, String password) throws Exception {
        // Establish a connection to the database
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Create a prepared statement to check if the user exists in the DBA_USERS table
            String sql = "SELECT username FROM dba_users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set the user parameter in the query
                stmt.setString(1, user.toUpperCase());

                // Execute the query
                try (ResultSet rs = stmt.executeQuery()) {
                    // Check if the user exists
                    if (rs.next()) {
                        System.out.println("Validation passed: User " + user + " exists.");
                    } else {
                        throw new Exception("Validation failed: User " + user + " does not exist.");
                    }
                }
            }
        } catch (Exception e) {
            // Handle any exceptions (e.g., database connection issues)
            System.err.println("Error during database validation: " + e.getMessage());
            throw e; // Rethrow the exception after logging it
        }
    }
}
