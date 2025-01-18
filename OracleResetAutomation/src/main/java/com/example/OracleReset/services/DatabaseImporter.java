package com.example.OracleReset.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DatabaseImporter {

    public static void importDatabase(String importCommand) throws IOException, InterruptedException {
        // Split the command into individual arguments for ProcessBuilder
        String[] commandArray = importCommand.split(" ");

        // Create a ProcessBuilder instance with the split command
        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

        // Redirect the error stream to merge with the standard output stream
        processBuilder.redirectErrorStream(true);

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
        } else {
            System.err.println("Database import failed with exit code: " + exitCode);
            System.err.println("Please check the import command and ensure the database is accessible.");
        }
    }
}
