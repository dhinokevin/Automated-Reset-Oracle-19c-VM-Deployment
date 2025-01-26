package com.example.OracleReset.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;
import java.io.File;

public class NotificationSender {

    private static String empdt = "stibotester2@gmail.com";  // Sender email address

    // Method to send email notifications with dynamic log file paths
    public static void sendEmail(List<String> mailIds, String subject, String body, String downloadPath) throws Exception {
        // Validate that the sender email address is not null or empty
        if (empdt == null || empdt.isEmpty()) {
            throw new IllegalStateException("Sender email (empdt) is not set.");
        }

        // Validate that mailIds is not null or empty
        if (mailIds == null || mailIds.isEmpty()) {
            throw new IllegalArgumentException("No recipient email addresses provided.");
        }

        // Set up properties for the SMTP server
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");  // Gmail SMTP host
        props.put("mail.smtp.port", "587");             // Port for TLS
        props.put("mail.smtp.auth", "true");            // Enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // Enable TLS encryption
        String sptal = "chinzuxqchvlcang"; // Your email password

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(empdt, sptal);
            }
        });

        try {
            // Loop through each email ID and send the email
            for (String recipient : mailIds) {
                // Validate that the recipient email is not null or empty
                if (recipient == null || recipient.isEmpty()) {
                    System.err.println("Skipping invalid recipient email address.");
                    continue;
                }

                // Create the email message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(empdt));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                message.setSubject(subject);

                // Create a multipart message for text and attachments
                Multipart multipart = new MimeMultipart();

                // Add the email body as the first part
                MimeBodyPart textBodyPart = new MimeBodyPart();
                textBodyPart.setText(body);
                multipart.addBodyPart(textBodyPart);

                // Dynamically set the log paths based on downloadPath
                String importLogPath = downloadPath + "\\import.log";
                String snapshotLogPath = downloadPath + "\\snapshot.log";

                // Add log file attachments
                addAttachment(multipart, importLogPath);
                addAttachment(multipart, snapshotLogPath);

                // Set the multipart content to the message
                message.setContent(multipart);

                // Send the email
                Transport.send(message);
                System.out.println("Email sent to: " + recipient);
            }

        } catch (MessagingException e) {
            // Handle any issues during email sending
            e.printStackTrace();
            throw new Exception("Failed to send email: " + e.getMessage(), e);
        }
    }

    // Helper method to add a file as an attachment
    private static void addAttachment(Multipart multipart, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                attachmentBodyPart.attachFile(file);
                multipart.addBodyPart(attachmentBodyPart);
                System.out.println("Attached file: " + filePath);
            } else {
                System.err.println("Attachment not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to attach file: " + filePath);
            e.printStackTrace();
        }
    }
}
