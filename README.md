# Automated Reset for Oracle 19c VM Deployment

This project provides a comprehensive end-to-end automated solution to reset Oracle 19c tenant environments. It integrates with Azure Blob Storage for seamless access to Data Pump files and provides API-based triggers for managing resets efficiently. Built with Java and Spring Boot, the solution adheres to high standards of automation, reliability, and configurability.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation and Setup](#installation-and-setup)
- [Process Overview](#process-overview)
- [Running the Project](#running-the-project)
- [Web Interface](#web-interface)
- [Troubleshooting](#troubleshooting)
- [Resources](#resources)
- [License](#license)
- [Contact](#contact)

## Features

- **API Integration**: Provides API endpoints to trigger the reset process and monitor its progress.
- **Blob Downloader**: Downloads Data Pump files from Azure Blob Storage using SAS tokens or Access Keys.
- **Snapshot Creation**: Creates snapshots of existing tenant schemas for rollback support.
- **Schema Management**: Securely drops and imports Oracle database schemas while preserving user privileges.
- **Validation**: Ensures schema existence and integrity.
- **Error Handling**: Robust error handling for database operations.
- **Notification**: Sends email notifications with logs and reports to multiple recipients.
- **Monitoring**: Tracks Blob Storage usage and provides detailed logs.
- **Rollback Management**: Manages rollback operations in case of failures.
- **Logging**: Generates separate log files for each process and attaches them to email notifications.
- **Highly Customizable**: Supports Access Key and SAS Token authentication, dynamic import/export configurations.
- **Credential Security**: Sensitive information is passed via API requests or environment variables.
- **Overall Security**: Added filters before connecting to the database to prevent attacks like SQL injection, along with entry and exit controls utilizing IP address ranging to provide multi-layer security.
- **specified Import**: Supports specified import such as Table and Schema during the import process.
- **Custom Directory**: Enables users to specify the download and import paths of the file for greater flexibility and control over file management.​

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Oracle Database 19c instance
- Azure SDK for Java
- SQL*Plus for executing SQL scripts
- Azure Blob Storage with a configured container
- Visual Studio Code with Java and Spring Boot extensions

## Installation and Setup

1. Clone the repository:
    ```bash
    git clone https://github.com/example/OracleReset.git
    cd OracleReset
    ```
2. Build the project:
    ```bash
    mvn clean install
    ```
3. Run the application:
    ```bash
    mvn spring-boot:run
    ```

## Process Overview

1. **Trigger Reset via API**: Sends a POST request to start the reset process.
2. **Download Backup**: Fetches the dump file from Azure Blob Storage.
3. **Create Snapshot**: Ensures rollback capability before schema deletion.
4. **Drop Schema**: Deletes the existing tenant schema.
5. **Import Schema**: Restores the database using `impdp`.
6. **Validate Schema**: Confirms the import was successful.
7. **Send Notifications**: Alerts stakeholders via email along with the log file.
8. **Monitor and Log**: Tracks progress and logs execution details.
9. **Rollback (If Needed)**: Reverts to the last known snapshot in case of failure.

## Running the Project

1. **Check Actuator Health Endpoint**:
    ```bash
    curl http://localhost:8080/actuator/health
    ```
2. **Trigger the Reset API**:
    ```bash
    curl -X POST http://localhost:8080/api/reset \
    -H "Content-Type: application/json" \
    -d '{ "connectionString":"Your_Connection_String", "sasToken":"Your_SAS_Token", "containerName":"your-container", "blobName":"your-blob.dmp", "adminName":"your-admin", "adminPassword":"your-admin-password", "tenantName":"your-tenant", "tenantPassword":"your-tenant-password", "mailIds":["email1@example.com", "email2@example.com"] }'
    ```

## Web Interface

A **user-friendly webpage** is provided to trigger the API request with a **multi-step form**.

- **Dynamic form validation** ensures required fields are filled.
- **Theming**: Light/Dark mode toggle.
- **Animations**: Includes falling leaves and star effects.
- **Progress tracking**: Visual progress bar.
- **Error handling and status messages**: Displays validation errors and success/failure messages.

### Running the Web Interface

1. Place `index.html`, `styles.css`, and `script.js` in the `static` folder inside your Spring Boot project.
2. Start the Spring Boot application.
3. Open `http://localhost:8080` in a browser.
4. Fill out the form and submit to trigger the reset process.

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

This project is licensed under the MIT License.

## Contact

For inquiries or support, please contact [bdk682003@gmail.com].
