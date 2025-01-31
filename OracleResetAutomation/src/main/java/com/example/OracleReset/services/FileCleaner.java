package com.example.OracleReset.services;

import java.io.File;

public class FileCleaner {

    // Method to check if the FULL_BACKUP.dmp file exists and delete it if present
    public static boolean deleteExistingBackupFile(String filePath) {
        // Create a File object
        File backupFile = new File(filePath);
        
        // Check if the file exists
        if (backupFile.exists()) {
            // Delete the file if it exists
            boolean deleted = backupFile.delete();
            if (deleted) {
                System.out.println("Existing backup file at " + filePath + " deleted successfully.");
            } else {
                System.out.println("Failed to delete the existing backup file at " + filePath);
            }
            return true; // File was deleted
        }
        
        // Return false if the file was not found
        return false;
    }

    // Method to check and delete the snapshot file for a given tenant name
    public static boolean deleteSnapshotFile(String tenantName) {
        // Define the file path for the snapshot file with the tenant name
        String filePath = "C:\\oracle\\base\\admin\\orcl\\dpdump\\snapshot_" + tenantName + ".dmp";
        
        // Create a File object
        File snapshotFile = new File(filePath);
        
        // Check if the file exists
        if (snapshotFile.exists()) {
            // Delete the file if it exists
            boolean deleted = snapshotFile.delete();
            if (deleted) {
                System.out.println("Existing snapshot file for tenant '" + tenantName + "' deleted successfully.");
            } else {
                System.out.println("Failed to delete the snapshot file for tenant '" + tenantName + "'.");
            }
            return true; // File was deleted
        }
        
        // Return false if the file was not found
        return false;
    }
}
