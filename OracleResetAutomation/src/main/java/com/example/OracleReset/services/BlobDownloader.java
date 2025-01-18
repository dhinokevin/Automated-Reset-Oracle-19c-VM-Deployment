package com.example.OracleReset.services;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BlobDownloader {

    public static void downloadBlob(String connectionString, String sasKey, String containerName, String blobName, String downloadPath) throws IOException {
        BlobServiceClient blobServiceClient = null;  // Declare the BlobServiceClient

        try {
            // Prioritize SAS Token if provided
            if (sasKey != null && !sasKey.isEmpty()) {
                System.out.println("Using SAS Token for authentication.");
                // Create a BlobServiceClient using SAS Token
                blobServiceClient = new BlobServiceClientBuilder()
                        .endpoint(sasKey)
                        .buildClient();
            } else if (connectionString != null && !connectionString.isEmpty()) {
                System.out.println("Using Access Key connection string for authentication.");
                // Create a BlobServiceClient using connection string
                blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();
            } else {
                throw new IllegalArgumentException("Neither SAS Token nor Access Key connection string is provided.");
            }

            // Get the container client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Check if the container exists
            if (!containerClient.exists()) {
                System.err.println("Error: Container not found: " + containerName);
                return;
            }

            // Get the blob client for the specified blob
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Check if the blob exists
            if (!blobClient.exists()) {
                System.err.println("Error: Blob not found: " + blobName);
                return;
            }

            // Check if the file already exists at the download path, and delete it if necessary
            File downloadFile = new File(downloadPath);
            if (downloadFile.exists()) {
                System.out.println("File already exists. Attempting to delete...");
                boolean deleted = deleteFileWithRetry(downloadFile);
                if (deleted) {
                    System.out.println("Existing file deleted: " + downloadPath);
                } else {
                    System.err.println("Failed to delete existing file: " + downloadPath);
                    return;
                }
            }

            // Validate the download path
            if (Files.notExists(Paths.get(downloadPath).getParent())) {
                System.out.println("Download path does not exist. Creating the path.");
                Files.createDirectories(Paths.get(downloadPath).getParent()); // Create parent directories if they don't exist
            }

            // Download the blob to the specified path
            blobClient.downloadToFile(downloadPath);
            System.out.println("Blob downloaded to: " + downloadPath);

        } catch (BlobStorageException e) {
            System.err.println("Azure Blob Storage error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File IO error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error downloading the blob: " + e.getMessage());
        }
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
                    e.printStackTrace();
                }
            }
        }
        return deleted;
    }
}
