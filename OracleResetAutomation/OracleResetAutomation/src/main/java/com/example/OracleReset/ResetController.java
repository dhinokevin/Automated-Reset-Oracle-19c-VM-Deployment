package com.example.OracleReset;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/reset")
@Validated
public class ResetController {

    private static final Logger logger = LoggerFactory.getLogger(ResetController.class);

    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping
    public ResponseEntity<String> triggerReset(@RequestBody ResetRequest resetRequest) {
        try {
            // Log the incoming request
            logger.info("Received reset request for tenant: {}", resetRequest.getTenantName());

            // Pass the request directly to the service layer
            resetService.resetDatabase(
                resetRequest.getConnectionString(), // Connection string for Access Key
                resetRequest.getSasToken(),        // SAS Token for authentication
                resetRequest.getContainerName(),   // Container name
                resetRequest.getBlobName(),        // Blob name
                resetRequest.getAdminName(),       // Admin username
                resetRequest.getAdminPassword(),   // Admin password
                resetRequest.getTenantName(),      // Tenant name
                resetRequest.getTenantPassword(),  // Tenant password
                resetRequest.getMailIds(),         // List of email IDs
                resetRequest.getDownloadPath(),    // Download path (null or provided)
                resetRequest.getSchemas()          // Comma-separated schemas (null or provided)
            );

            logger.info("Database reset process started successfully for tenant: {}", resetRequest.getTenantName());

            // Return 200 OK with success message
            return ResponseEntity.ok("Database reset process started successfully.");
        } catch (IllegalArgumentException e) {
            // Handle validation errors or bad inputs (400 Bad Request)
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors (500 Internal Server Error)
            logger.error("Error during database reset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during database reset: " + e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception e) {
        logger.error("Unhandled error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }
}
