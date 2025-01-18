package com.example.OracleReset.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RollbackManager {
    public static void rollbackDatabase(String command) throws IOException, InterruptedException {
        // Create a new ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));  // Split the command into arguments
        processBuilder.inheritIO();  // To show the process output in the console

        // Start the process
        Process process = processBuilder.start();

        // Capture and print standard error stream for better debugging
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();

        // Check if the process completed successfully
        if (exitCode == 0) {
            System.out.println("Rollback completed!");
        } else {
            System.err.println("Rollback failed with exit code: " + exitCode);
        }
    }
}
