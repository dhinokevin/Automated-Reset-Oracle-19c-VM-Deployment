package com.example.OracleReset.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseValidator {

    // Modify the method to accept additional flags for previous steps
    public static boolean validateDatabase(String url, String user, String password, boolean isBlobDownloaded,
                                            boolean isDatabaseDropped, boolean isDatabaseImported) {
        // Start by assuming validation fails
        boolean isValid = false;

        // Perform validation only if all previous steps have succeeded
        if (isBlobDownloaded && isDatabaseDropped && isDatabaseImported) {
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
                            isValid = true; // Validation succeeded
                        } else {
                            System.err.println("Validation failed: User " + user + " does not exist.");
                        }
                    }
                }
            } catch (Exception e) {
                // Handle any exceptions (e.g., database connection issues)
                System.err.println("Error during database validation: " + e.getMessage());
            }
        } else {
            // Provide detailed feedback on which step failed
            if (!isBlobDownloaded) {
                System.err.println("Validation skipped: Blob download failed.");
            }
            if (!isDatabaseDropped) {
                System.err.println("Validation skipped: Database drop failed.");
            }
            if (!isDatabaseImported) {
                System.err.println("Validation skipped: Database import failed.");
            }
        }

        return isValid; // Return the validation result
    }
}
