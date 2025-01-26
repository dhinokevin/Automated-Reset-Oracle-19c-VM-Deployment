# Automated Reset for Oracle 19c VM Deployment

This project provides a comprehensive end-to-end automated solution to reset Oracle 19c tenant environments. It integrates with Azure Blob Storage for seamless access to Data Pump files and provides API-based triggers for managing resets efficiently. Built with Java and Spring Boot, the solution adheres to high standards of automation, reliability, and configurability.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation and Setup](#installation-and-setup)
- [Process Overview](#process-overview)
- [Running the Project](#running-the-project)
- [Troubleshooting](#troubleshooting)
- [Resources](#resources)
- [License](#license)
- [Contact](#contact)

## Features

- **API Integration**: Provides API endpoints to trigger the reset process and monitor its progress. This REST API request makes it easier to automate, keeps credentials dynamic and secure, and makes the project customizable for various use cases.
- **Blob Downloader**: Downloads blobs (e.g., Data Pump files) from Azure Blob Storage. The process supports both SAS tokens and Access Keys for authenticating and accessing the blob storage.
- **Snapshot Creation**: Creates snapshots of existing tenant schemas for rollback support.
- **Schema Management**: Securely drops and imports Oracle database schemas. The schema is dropped without affecting the user or their privileges, ensuring that the user remains intact and their privileges are preserved. This allows for a smooth reset of the schema while maintaining the integrity of the user setup.
- **Validation**: Validates schema existence and integrity.
- **Error Handling**: Robust error handling for database drop, file access, and import steps.
- **Notification**: Sends email notifications with logs and reports. Multiple email addresses can be specified to notify stakeholders about the status of the process, including success or failure.
- **Monitoring**: Tracks and reports Blob Storage usage and provides detailed logs.
- **Rollback Management**: Manages rollback operations in case of failures.
- **Logging**: Each process generates a separate log file that is attached to the email notifications, whether the project completes successfully or fails. This helps track all activities and simplifies troubleshooting.
- **Highly Customizable**: Supports both Access Key and SAS Token for Azure Blob Storage connectivity, dynamically adapts import/export commands based on configuration and blob names.
- **Credential Security**: No credentials are hardcoded in the project. All credentials, including database connection strings, passwords, and Azure SAS tokens, are passed dynamically via API requests or environment variables, ensuring that sensitive information remains secure. This prevents accidental exposure of credentials and ensures compliance with best security practices.
- **Schema Remapping**: Includes support for remapping schemas during the import process (e.g., `remap_schema=old_schema:new_schema`). Use this feature only when both the old and new schemas are known and correctly specified. If not, it is recommended to skip this functionality and allow the code to handle the process automatically.

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Oracle Database 19c instance
- Azure SDK: For interacting with Azure services like Blob Storage.
- SQL*Plus: Required for executing SQL scripts and performing administrative tasks directly with the Oracle database.
- Azure Blob Storage with a configured container name

### VS Extension

#### Java Extension Pack
This pack includes several key extensions to work with Java:
- **Language Support for Java(TM) by Red Hat**: Provides Java language support including syntax highlighting, IntelliSense, and more.
- **Debugger for Java**: Allows debugging Java applications within VS Code.
- **Java Test Runner**: Helps run and debug Java tests.
- **Maven for Java**: Provides support for Maven, which is essential for building the project.
- **Java Dependency Viewer**: Visualizes and manages your Java project dependencies.

#### Spring Boot Extension Pack
- **Spring Boot Tools**: Provides support for running and debugging Spring Boot applications.
- **Spring Initializr Java Support**: Quickly generate Spring Boot projects using Spring Initializr.
  
#### Azure Extensions
- **Azure Tools**: Integrates VS Code with Azure, allowing you to manage resources like Blob Storage and virtual machines directly from the editor.
- **Azure Storage**: Allows interacting with Azure Blob Storage directly from VS Code. Useful for managing and accessing Data Pump files stored in Azure.
- **Azure Functions**: If you're planning to integrate any serverless functions, this extension is helpful.
- **Azure CLI Tools**: Integrates Azure CLI commands into your VS Code environment.

## Installation and Setup

### Steps

1. Clone the repository:

    ```bash
    git clone https://github.com/example/OracleReset.git
    cd OracleReset
    ```

2. Build the project:

    ```bash
    mvn clean install
    ```

## Process Overview

1. **REST API Request to Start the Reset Process**: The reset process can be started by sending a POST request to the API. This allows for a dynamic and automated way to trigger the reset process without manual intervention.

2. **Download Backup**: Downloads the dump file from Azure Blob Storage to the local directory.

3. **Create Snapshot**: Before dropping the tenant schema, a snapshot is created to ensure rollback capability.

4. **Drop Schema**: Drops the existing schema of the tenant to prepare for a new import.

5. **Import Schema**: Automates the Data Pump import process using `impdp` to restore the database from the Data Pump file. The backend code also includes the ability to remap schemas during the import process. The import parameters (e.g., `remap_schema=old_schema:new_schema`) are defined internally in the application and are dynamically added to the `impdp` command.

    Example (remapping schema):
    ```bash
    remap_schema=old_schema:new_schema
    ```

    **Note**: The import process will handle schema remapping if required, based on predefined configurations or dynamically passed parameters. This functionality should only be used when both the old and new schema parameters are correctly specified. If not, it is better to skip this functionality and allow the code to handle the process automatically.

6. **Validate Schema**: Confirms that the schema was imported correctly and is in a valid state.

7. **Notify Stakeholders**: Sends email notifications to stakeholders about the success or failure of the process. Multiple email addresses can be specified to notify multiple stakeholders as needed. This is configurable through the API request.

8. **Monitor and Log**: Tracks Blob Storage usage and logs detailed progress and error information.

9. **Rollback (If Needed)**: If the process fails, it rolls back to the last known snapshot of the tenant schema.

## Running the Project

1. **Build the application**:

    ```bash
    mvn clean install
    ```

2. **Run the application**:

    ```bash
    mvn exec:java
    ```

3. **Check Actuator Health Endpoint**:  
    Before triggering the reset process, it is a good practice to ensure that the Spring Boot application is up and running. You can check the health status of the application using the Spring Boot Actuator's health endpoint.

    Use the following `curl` command to check the health status:

    ```bash
    curl http://localhost:8080/actuator/health
    ```

4. **Trigger the Reset API**:  
    Use the following `curl` command to initiate the reset process:

    ```bash
    curl -X POST http://localhost:8080/api/reset \
    -H "Content-Type: application/json" \
    -d '{
        "connectionString":"Your_Connection_String",
        "sasToken":"Your_SAS_Token",
        "containerName":"your-container",
        "blobName":"your-blob.dmp",
        "adminName":"your-admin",
        "adminPassword":"your-admin-password",
        "tenantName":"your-tenant",
        "tenantPassword":"your-tenant-password",
        "mailIds":["email1@example.com", "email2@example.com"]
    }'
    ```

    **Note**: Replace placeholders like `Your_Connection_String` and `Your_SAS_Token` with your actual credentials. You can specify multiple email addresses in the `mailIds` array. Ensure the admin account has `SYSDBA` access.

## Troubleshooting

If you encounter any issues, follow these steps to diagnose the problem:

1. **Check the Terminal Output**: The terminal where the application is running will provide detailed error messages for each step of the process. This helps identify what went wrong, whether it's a connection issue, a problem with the schema import, or an issue with Azure Blob Storage.

2. **Check the .log File**: Each process generates a log file. This log file contains detailed information about the execution of the project, including success and error messages. The log file is also attached to the email notifications sent at the end of the process. Checking the log file will help provide more clarity on the error.

Some common errors and troubleshooting steps:

- **Error: Unable to connect to Azure Blob Storage**:
    - Ensure the `connectionString` or `sasToken` is correct and has the necessary permissions (e.g., read, write, or list).

- **Error: Database schema import failed**:
    - Verify that the correct schema file is present in the blob storage and is accessible.
    - Check the logs for specific error details that can guide further debugging.

- **Error: Unable to connect to Oracle Database**:
    - Ensure the database instance is running and accessible.
    - Verify that the connection string is correct and the credentials provided are valid.

- **Error: Invalid or Missing SAS Token**:
    - Confirm the SAS token provided has the necessary permissions to access the Azure Blob Storage container and blob.

## Resources

- **Oracle Database 19c**:
    - [Oracle Database Documentation 19c](https://www.oracle.com/database/technologies/)
    - [Oracle Data Pump Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/19/dpump/)

- **Java and Spring Boot**:
    - [Official Java Documentation](https://docs.oracle.com/en/java/)
    - [Spring Boot Documentation](https://spring.io/projects/spring-boot)
    - [Spring Guides](https://spring.io/guides/)

- **Azure Blob Storage and Azure SDK for Java**:
    - [Azure Blob Storage Documentation](https://learn.microsoft.com/en-us/azure/storage/blobs/)
    - [Azure SDK for Java Documentation](https://learn.microsoft.com/en-us/java/azure/?view=azure-java-stable)
    - [Azure Blob Storage Event Grid Trigger (Java)](https://learn.microsoft.com/en-us/azure/event-grid/first-event-grid-trigger-function-java)

- **SQL Resources**:
    - [Oracle SQL Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/)
    - [SQL Tutorial](https://www.w3schools.com/sql/)

- **REST APIs and Web Services**:
    - [REST API Tutorial](https://restfulapi.net/)
    - [Building a RESTful Web Service with Spring Boot](https://spring.io/guides/gs/rest-service/)

- **Logging and Monitoring**:
    - [Azure Monitor Documentation](https://learn.microsoft.com/en-us/azure/azure-monitor/)
    - [Spring Boot Logging Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)


## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For inquiries or support, please contact [bdk682003@gmail.com].
