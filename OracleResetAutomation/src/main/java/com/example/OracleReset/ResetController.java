package com.example.OracleReset;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/reset")
public class ResetController {

    private static final Logger logger = LoggerFactory.getLogger(ResetController.class);

    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping
    public ResponseEntity<String> triggerReset(@RequestBody ResetRequest resetRequest) {
        try {
            logger.info("Received reset request for tenant: {}", resetRequest.getTenantName());
            resetService.resetDatabase(
                resetRequest.getConnectionString(), // Connection string for Access Key
                resetRequest.getSasToken(),        // SAS Token for authentication
                resetRequest.getContainerName(),   // Container name
                resetRequest.getBlobName(),        // Blob name
                resetRequest.getAdminName(),       // Admin username
                resetRequest.getAdminPassword(),   // Admin password
                resetRequest.getTenantName(),      // Tenant name
                resetRequest.getTenantPassword(),  // Tenant password
                resetRequest.getMailIds()          // Pass the list of email IDs
            );
            logger.info("Database reset process started successfully for tenant: {}", resetRequest.getTenantName());
            return ResponseEntity.ok("Database reset process started successfully.");
        } catch (Exception e) {
            logger.error("Error during database reset: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error during database reset: " + e.getMessage());
        }
    }
}
