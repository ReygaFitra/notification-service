package com.reyga_dev.notification_service.application;

import com.reyga_dev.notification_service.domain.dto.EmailRequest;

public interface INotificationProviderService {

    void email(String eventId, EmailRequest request, boolean requireAttachment);

}
