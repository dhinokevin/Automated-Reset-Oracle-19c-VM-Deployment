package com.example.OracleReset.services;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobStorageException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.net.URL;

public class BlobDownloader {

    public static boolean downloadBlob(String connectionString, String sasURL, String containerName, String blobName, String downloadPath) {
        // First attempt to delete the existing backup file using FileCleaner
        if (FileCleaner.deleteExistingBackupFile(downloadPath)) {
            System.out.println("Existing file at the path " + downloadPath + " deleted successfully.");
        } else {
            System.out.println("Failed to delete the existing file at the path: " + downloadPath);
            // If deletion fails, attempt to delete with retry
            File downloadFile = new File(downloadPath);
            boolean deleted = deleteFileWithRetry(downloadFile);
            if (!deleted) {
                System.err.println("Failed to delete existing file after retry attempts.");
                return false; // Return false if the file couldn't be deleted after retries
            }
            System.out.println("File deleted successfully after retry.");
        }

        try {
            if (sasURL != null && !sasURL.isEmpty()) {
                System.out.println("Using SAS URL for authentication.");
                
                URI uri = URI.create(sasURL);  // Create a URI from the SAS URL string
                URL url = uri.toURL();         // Convert the URI to a URL
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(downloadPath);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();

                // Validate the download path
                if (Files.notExists(Paths.get(downloadPath).getParent())) {
                    System.out.println("Download path does not exist. Creating the path.");
                    Files.createDirectories(Paths.get(downloadPath).getParent()); // Create parent directories if they don't exist
                }

                System.out.println("Blob downloaded to: " + downloadPath);
                return true;

            } else if (connectionString != null && !connectionString.isEmpty()) {
                System.out.println("Using Access Key connection string for authentication.");
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();
                
                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
                if (!containerClient.exists()) {
                    System.err.println("Error: Container not found: " + containerName);
                    return false;
                }

                BlobClient blobClient = containerClient.getBlobClient(blobName);
                if (!blobClient.exists()) {
                    System.err.println("Error: Blob not found: " + blobName);
                    return false;
                }

                blobClient.downloadToFile(downloadPath);
                System.out.println("Blob downloaded to: " + downloadPath);
                return true;
            } else {
                throw new IllegalArgumentException("Neither SAS URL nor Access Key connection string is provided.");
            }
        } catch (BlobStorageException e) {
            System.err.println("Azure Blob Storage error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File IO error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error downloading the blob: " + e.getMessage());
        }
        
        return false; // Return false in case of any error
    }

    // Retry deletion for 3 times if file cannot be deleted immediately
    private static boolean deleteFileWithRetry(File file) {
        int retries = 3;
        boolean deleted = false;
        while (retries > 0 && !deleted) {
            deleted = file.delete();
            if (!deleted) {
                System.out.println("Retrying file deletion...");
                retries--;
                try {
                    Thread.sleep(1000); // Wait for 1 second before retrying
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    e.printStackTrace();
                }
            }
        }
        return deleted;
    }
}
