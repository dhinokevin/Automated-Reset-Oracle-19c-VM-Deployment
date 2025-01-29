package com.example.OracleReset;

import java.util.List;

public class ResetRequest {
    private String connectionString;
    private String sasToken;
    private String adminName;
    private String adminPassword;
    private String tenantName;
    private String tenantPassword;
    private String containerName;
    private String blobName;
    private List<String> mailIds;
    private String schemas;       // New field for schemas
    private String downloadPath;  // New field for custom download path

    // Getters and Setters
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getSasToken() {
        return sasToken;
    }

    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantPassword() {
        return tenantPassword;
    }

    public void setTenantPassword(String tenantPassword) {
        this.tenantPassword = tenantPassword;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public List<String> getMailIds() {
        return mailIds;
    }

    public void setMailIds(List<String> mailIds) {
        this.mailIds = mailIds;
    }

    public String getSchemas() { // Getter for schemas
        return schemas;
    }

    public void setSchemas(String schemas) { // Setter for schemas
        this.schemas = schemas;
    }

    public String getDownloadPath() { // Getter for download path
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) { // Setter for download path
        this.downloadPath = downloadPath;
    }
}
