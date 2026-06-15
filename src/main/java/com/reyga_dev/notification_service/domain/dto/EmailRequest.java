package com.reyga_dev.notification_service.domain.dto;

import java.util.List;

public record EmailRequest(
        String to, String subject, String text, String cc, String bcc, boolean html, List<Attachment> attachments
) {
    public record Attachment(
            String fileName,
            String storageType, String bucketName,
            String objectKey, String contentType, Long size
    ) {}
}
