package com.example.OracleReset.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseImporter {

    public static boolean importDatabase(String username, String password, String blobName, 
                                         String schemas, boolean isBlobDownloaded,String ORACLE_CONNECTION_STRING) {
        // Step 1: Check if the blob was downloaded successfully
        if (!isBlobDownloaded) {
            System.err.println("Blob download was not successful. Skipping database import.");
            return false;
        }

        // Step 2: Check if the database connection is valid
        if (!isDatabaseConnectionValid(ORACLE_CONNECTION_STRING,username, password)) {
            System.err.println("Invalid database connection. Skipping database import.");
            return false;
        }

        // Step 3: Form the import command based on the presence of schemas
        String importCommand;
        if (schemas != null && !schemas.trim().isEmpty()) {
            importCommand = String.format(
                "impdp %s/%s directory=data_pump_dir dumpfile=%s schemas=%s content=all table_exists_action=replace",
                username, password, blobName, schemas.trim()
            );
            System.out.println("Importing specific schemas: " + schemas.trim());
        } else {
            importCommand = String.format(
                "impdp %s/%s directory=data_pump_dir dumpfile=%s content=all table_exists_action=replace",
                username, password, blobName
            );
            System.out.println("Importing the entire dump file...");
        }

        // Step 4: Perform the database import
        return executeImportCommand(importCommand);
    }

    // Method to check database connection
    private static boolean isDatabaseConnectionValid(String ORACLE_CONNECTION_STRING,String username, String password) {
        try (Connection connection = DriverManager.getConnection(ORACLE_CONNECTION_STRING, username, password)) {
            if (connection != null) {
                System.out.println("Database connection successful.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return false; // Connection failed
    }

    // Method to execute the import command
    private static boolean executeImportCommand(String importCommand) {
        // Split the command into individual arguments for ProcessBuilder
        String[] commandArray = importCommand.split(" ");

        // Create a ProcessBuilder instance with the split command
        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

        // Redirect the error stream to merge with the standard output stream
        processBuilder.redirectErrorStream(true);

        try {
            // Start the process
            Process process = processBuilder.start();

            // Capture and print output (both standard and error due to redirectErrorStream)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // Print combined standard and error output
                }
            }

            // Wait for the process to complete and get the exit code
            int exitCode = process.waitFor();

            // Check if the process completed successfully
            if (exitCode == 0) {
                System.out.println("Database import completed successfully!");
                return true; // Import was successful
            } else {
                System.err.println("Database import failed with exit code: " + exitCode);
                return false; // Import failed
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error during database import: " + e.getMessage());
            return false; // Error occurred during the import process
        }
    }
}
