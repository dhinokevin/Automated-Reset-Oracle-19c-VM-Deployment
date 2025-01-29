package com.example.OracleReset.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DatabaseDropper {

    public static boolean dropDatabase(String url, String user, String password, String tenant) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             @SuppressWarnings("unused")
            Statement stmt = conn.createStatement()) {

            // Step 1: Check if the connection is valid
            if (!isValidConnection(conn)) {
                System.err.println("Invalid username/password; logon denied");
                return false;
            }

            // Step 2: Create a full database dump for the tenant
            boolean isDumpCreated = createDatabaseDump(url, password, tenant);
            if (!isDumpCreated) {
                System.err.println("Failed to create database dump for tenant: " + tenant);
                return false;
            }

            // SQL script to kill the tenant user sessions
            String dropSQL = "BEGIN " +
                "FOR c IN (SELECT sid, serial# FROM v$session WHERE username = ?) LOOP " +
                "EXECUTE IMMEDIATE 'ALTER SYSTEM KILL SESSION ''' || c.sid || ',' || c.serial# || ''' IMMEDIATE'; " +
                "END LOOP; " +
                "END;";

            // Execute the SQL (this will kill the sessions for the tenant user)
            try (PreparedStatement ps = conn.prepareStatement(dropSQL)) {
                ps.setString(1, tenant.toUpperCase());  // Use the tenant as the username in uppercase
                ps.execute();
                System.out.println("Sessions for user " + tenant + " have been killed successfully!");
            }

            return true;

        } catch (Exception e) {
            // Handle the exception and print an error message
            System.err.println("An error occurred while processing the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isValidConnection(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Execute a simple query to verify the connection
            stmt.executeQuery("SELECT 1 FROM dual");
            return true;
        } catch (Exception e) {
            // If the query fails, it indicates an invalid connection
            System.err.println("Connection verification failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean createDatabaseDump(String url, String password, String tenant) {
        System.out.println("Creating a full database dump for tenant: " + tenant);

        // Path to the dump file on the server (Ensure the directory is valid and has write permissions)
        String dumpFilePath = "data_pump_dir:snapshot_" + tenant + ".dmp"; // Oracle Data Pump directory with tenant-specific name
        System.out.println("Created Snapshot will be: " + dumpFilePath);

        // Delete any existing snapshot file
        if (FileCleaner.deleteSnapshotFile(tenant)) {
            System.out.println("Snapshot file for tenant '" + tenant + "' deleted.");
        } else {
            System.out.println("No snapshot file for tenant '" + tenant + "' found.");
        }

        // Step 2: Build the Data Pump export command
        String expdpCommand = "expdp " + tenant + "/" + password + " DIRECTORY=data_pump_dir DUMPFILE=snapshot_" + tenant + ".dmp LOGFILE=snapshot.log SCHEMAS=" + tenant + " CONTENT=ALL";

        // Use ProcessBuilder to execute the expdp command
        ProcessBuilder processBuilder = new ProcessBuilder(expdpCommand.split(" "));
        processBuilder.inheritIO(); // Inherit IO to print output to console

        try {
            // Start the process
            Process process = processBuilder.start();

            // Capture output from the process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // Print standard output
                }
            }

            // Capture error output (if any)
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine); // Print error output
                }
            }

            // Wait for the process to finish and check the exit code
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Database dump (snapshot_" + tenant + ".dmp) created successfully!");
                return true;
            } else {
                System.err.println("Error occurred while creating the database dump. Exit code: " + exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error during the dump creation process: " + e.getMessage());
            return false;
        }
    }
}
