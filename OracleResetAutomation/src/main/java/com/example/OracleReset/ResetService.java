package com.example.OracleReset;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.OracleReset.services.BlobDownloader;
import com.example.OracleReset.services.DatabaseDropper;
import com.example.OracleReset.services.DatabaseImporter;
import com.example.OracleReset.services.DatabaseValidator;
import com.example.OracleReset.services.NotificationSender;
import com.example.OracleReset.services.RollbackManager;

@Service
public class ResetService {

    // Method for performing the entire reset process with dynamic inputs
    public void resetDatabase(String connectionString, String sasToken, String containerName, String blobName, String adminName, String adminPassword, String tenantName, String tenantPassword, List<String> mailIds) throws Exception {
        try {
            // Step 1: Download backup from Azure Blob Storage
            System.out.println("Downloading backup from Azure Blob Storage...");
            String downloadPath = "C:\\oracle\\base\\admin\\orcl\\dpdump\\" + blobName;
            BlobDownloader.downloadBlob(connectionString, sasToken, containerName, blobName, downloadPath);

            // Step 2: Drop the existing database for the specified tenant
            System.out.println("Dropping the existing database...");
            DatabaseDropper.dropDatabase("jdbc:oracle:thin:@localhost:1521:ORCL", adminName, adminPassword, tenantName);


            // Step 3: Import the database using Data Pump
            String importCommand = String.format("impdp %s/%s directory=data_pump_dir dumpfile=%s content=all table_exists_action=replace", 
                                                  tenantName, tenantPassword, blobName);
            System.out.println("Importing the database...");
            DatabaseImporter.importDatabase(importCommand);

            // Step 4: Validate the reset
            System.out.println("Validating the reset...");
            DatabaseValidator.validateDatabase("jdbc:oracle:thin:@localhost:1521:ORCL", tenantName, tenantPassword);

            // Step 5: Notify admin of successful reset
            System.out.println("Notifying admin...");
            NotificationSender.sendEmail(mailIds, "Reset Complete", "Oracle database reset was successful.");

        } catch (Exception e) {
            // Handle errors and perform rollback if needed
            System.err.println("Error during database reset: " + e.getMessage());
            try {
                // Notify admin of failure
                NotificationSender.sendEmail(mailIds, "Reset Failed", "The database reset failed. Error: " + e.getMessage());

                // Perform a rollback
                System.out.println("Rolling back the database...");
                String rollbackCommand = String.format("impdp %s/%s DIRECTORY=data_pump_dir DUMPFILE=snapshot_%s.dmp schemas=%s content=all table_exists_action=replace", 
                                                      tenantName, tenantPassword, tenantName, tenantName);
                RollbackManager.rollbackDatabase(rollbackCommand);
            } catch (Exception rollbackException) {
                // Log rollback failure
                System.err.println("Rollback also failed: " + rollbackException.getMessage());
            }
        }
    }
}
