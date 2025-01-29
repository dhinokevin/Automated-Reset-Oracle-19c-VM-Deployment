package com.example.OracleReset;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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

    private static final String DEFAULT_DOWNLOAD_PATH = "C:\\oracle\\base\\admin\\orcl\\dpdump";
    private static final String ORACLE_CONNECTION_STRING = "jdbc:oracle:thin:@localhost:1521:ORCL";

    public void resetDatabase(String connectionString, String sasToken, String containerName, String blobName,
                              String adminName, String adminPassword, String tenantName, String tenantPassword,
                              List<String> mailIds, String downloadPath, String schemas) throws Exception {
        boolean dataDumpCompleted = false;
        boolean isBlobDownloaded = false;
        boolean isDatabaseDropped = false;
        boolean isDatabaseImported = false;
        boolean isDatabaseValid = false;
        StringBuilder failureDetails = new StringBuilder();

        try {
            // Step 1: Determine the download path
            if (downloadPath == null || downloadPath.isEmpty()) {
                downloadPath = DEFAULT_DOWNLOAD_PATH; // Use default path if no custom path is provided
                System.out.println("Using default download path: " + downloadPath);
            } else {
                System.out.println("Using custom download path: " + downloadPath);

                // Configure the custom path as DATA_PUMP_DIR
                System.out.println("Configuring DATA_PUMP_DIR...");
                setDataPumpDirectory(downloadPath, adminName, adminPassword);
            }

            // Step 2: Download blob to the specified path
            String blobDownloadPath = downloadPath + "\\" + blobName;
            System.out.println("Downloading backup from Azure Blob Storage...");
            isBlobDownloaded = BlobDownloader.downloadBlob(connectionString, sasToken, containerName, blobName, blobDownloadPath);

            if (!isBlobDownloaded) {
                failureDetails.append("Blob download failed.\n");
                throw new Exception("Blob download failed. Exiting the reset process.");
            }

            // Step 3: Drop the existing database for the specified tenant
            System.out.println("Dropping the existing database...");
            isDatabaseDropped = DatabaseDropper.dropDatabase(ORACLE_CONNECTION_STRING, adminName, adminPassword, tenantName);

            // Step 4: Import the database using Data Pump
            isDatabaseImported = DatabaseImporter.importDatabase(tenantName, tenantPassword, blobName, schemas, isBlobDownloaded, ORACLE_CONNECTION_STRING);
            dataDumpCompleted = true; // Mark data dump as completed

            // Step 5: Validate the reset
            System.out.println("Validating the reset...");
            isDatabaseValid = DatabaseValidator.validateDatabase(ORACLE_CONNECTION_STRING, tenantName, tenantPassword, isBlobDownloaded, isDatabaseDropped, isDatabaseImported);

            // Step 6: Notify admin of successful reset
            System.out.println("Notifying admin...");
            if (isDatabaseValid) {
                NotificationSender.sendEmail(mailIds, "Reset Complete", "Oracle database reset was successful.", downloadPath);
            } else {
                failureDetails.append("The following step(s) failed:\n");
                if (!isBlobDownloaded) failureDetails.append("Blob download failed.\n");
                if (!isDatabaseDropped) failureDetails.append("Database drop failed.\n");
                if (!isDatabaseImported) failureDetails.append("Database import failed.\n");
                if (!isDatabaseValid) failureDetails.append("Database validation failed.\n");
                NotificationSender.sendEmail(mailIds, "Reset Failed", "Oracle database reset was unsuccessful. " + failureDetails.toString(), downloadPath);
            }

        } catch (Exception e) {
            System.err.println("Error during database reset: " + e.getMessage());

            if (!dataDumpCompleted) {
                failureDetails.append("Critical error during blob download or pre-data-dump steps: " + e.getMessage() + "\n");
                // Notify the admin of the failure
                NotificationSender.sendEmail(mailIds, "Reset Failed", "Critical error during the database reset process. Error details: \n" + failureDetails.toString(), downloadPath);
                throw new Exception("Critical error during blob download or pre-data-dump steps: " + e.getMessage(), e);
            }

            try {
                // Notify admin of failure and provide detailed failure details
                failureDetails.append("The following step(s) failed:\n");
                if (!isBlobDownloaded) failureDetails.append("Blob download failed.\n");
                if (!isDatabaseDropped) failureDetails.append("Database drop failed.\n");
                if (!isDatabaseImported) failureDetails.append("Database import failed.\n");
                if (!isDatabaseValid) failureDetails.append("Database validation failed.\n");

                NotificationSender.sendEmail(mailIds, "Reset Failed", "The database reset failed. Error details: \n" + failureDetails.toString(), downloadPath);

                // Perform a rollback using snapshot import
                System.out.println("Rolling back the database using snapshot...");
                String rollbackCommand = String.format(
                    "impdp %s/%s DIRECTORY=data_pump_dir DUMPFILE=snapshot_%s.dmp schemas=%s content=all table_exists_action=replace",
                    tenantName, tenantPassword, tenantName, tenantName
                );
                RollbackManager.rollbackDatabase(rollbackCommand);

            } catch (Exception rollbackException) {
                System.err.println("Rollback also failed: " + rollbackException.getMessage());
                failureDetails.append("Rollback failed: " + rollbackException.getMessage() + "\n");
                NotificationSender.sendEmail(mailIds, "Rollback Failed", "Rollback attempt also failed. Error details: \n" + failureDetails.toString(), downloadPath);
            }
        }
    }

    // Method to set the DATA_PUMP_DIR dynamically
    private void setDataPumpDirectory(String directoryPath, String adminName, String adminPassword) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(ORACLE_CONNECTION_STRING, adminName, adminPassword);
            statement = connection.createStatement();

            // Drop the existing directory if it exists
            try {
                statement.executeUpdate("DROP DIRECTORY data_pump_dir");
            } catch (Exception e) {
                // Ignore if the directory does not exist
            }

            // Create the new directory
            String createDirQuery = String.format("CREATE DIRECTORY data_pump_dir AS '%s'", directoryPath.replace("\\", "/"));
            statement.executeUpdate(createDirQuery);

            // Grant necessary privileges
            String grantPrivilegesQuery = "GRANT READ, WRITE ON DIRECTORY data_pump_dir TO PUBLIC";
            statement.executeUpdate(grantPrivilegesQuery);

            System.out.println("DATA_PUMP_DIR successfully configured.");
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
