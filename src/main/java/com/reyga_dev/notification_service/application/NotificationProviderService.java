package com.reyga_dev.notification_service.application;

import com.reyga_dev.notification_service.common.ResilienceServices;
import com.reyga_dev.notification_service.config.properties.EmailConfigProperties;
import com.reyga_dev.notification_service.domain.dto.EmailRequest;
import com.reyga_dev.notification_service.domain.enums.NotificationDeliveryStatus;
import com.reyga_dev.notification_service.domain.exception.NotificationProcessingException;
import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationDelivery;
import com.reyga_dev.notification_service.infrastucture.persistance.repository.NotificationDeliveryRepository;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.OffsetDateTime;

@Service
public class NotificationProviderService implements INotificationProviderService{

    private static final Logger log = LoggerFactory.getLogger(NotificationProviderService.class);

    private final JavaMailSender javaMailSender;
    private final RetryConfig retryConfig;
    private final RetryRegistry retryRegistry;
    private final ResilienceServices resilienceServices;
    private final EmailConfigProperties emailConfigProperties;
    private final NotificationDeliveryRepository notificationDeliveryRepository;

    public NotificationProviderService(JavaMailSender javaMailSender, RetryConfig retryConfig, RetryRegistry retryRegistry, ResilienceServices resilienceServices,
                                       EmailConfigProperties emailConfigProperties, NotificationDeliveryRepository notificationDeliveryRepository) {
        this.javaMailSender = javaMailSender;
        this.retryConfig = retryConfig;
        this.retryRegistry = retryRegistry;
        this.resilienceServices = resilienceServices;
        this.emailConfigProperties = emailConfigProperties;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    @Override
    @Transactional
    public void email(String eventId, EmailRequest request, boolean requireAttachment) {
        log.info("[Executing Mail Service] ---> Event Id: {}", eventId);

        MimeMessage message = this.constructEmailMessage(eventId, request, requireAttachment);

        try {
            resilienceServices.useRetry(retryConfig, retryRegistry, "email-service", () ->
                    this.sendEmailMessage(eventId, request, message)
            );
            this.updateSentNotificationDeliveryStatus(eventId);
        } catch (RuntimeException ex) {
            Throwable failureCause = ex.getCause() == null ? ex : ex.getCause();
            log.error(
                    "[Mail Service] ---> Mail sending failed after retry [Event Id: {}, maxAttempts: {}]",
                    eventId, retryConfig.getMaxAttempts()
            );
            this.updateFailedNotificationDeliveryStatus(eventId, failureCause);
        }
    }

    private MimeMessageHelper constructBaseMessageHelper(EmailRequest request, MimeMessage message) throws MessagingException {
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
        messageHelper.setFrom(emailConfigProperties.from());
        messageHelper.setTo(request.to());
        messageHelper.setSubject(request.subject());
        messageHelper.setText(request.text(), request.html());

        return messageHelper;
    }

    private MimeMessage constructEmailMessage(String eventId, EmailRequest request, boolean requireAttachment) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = this.constructBaseMessageHelper(request, message);

            if (request.cc() != null && !request.cc().isBlank()) messageHelper.setCc(request.cc());
            if (request.bcc() != null && !request.bcc().isBlank()) messageHelper.setBcc(request.bcc());

            // todo: Commenting this section temporary, waiting for notification library developmentS
            /*
            *
            if (requireAttachment) {
                for (EmailRequest.Attachment attachment : request.attachments()) {
                    messageHelper.addAttachment(attachment.fileName(), new FileSystemResource(new File(attachment.path())));
                }
            }
            *
            */

            return message;
        } catch (MessagingException ex) {
            log.error("[MessagingException] ---> Failed to build email message. eventId={}", eventId);
            throw new NotificationProcessingException("Failed to build email message", ex);
        }
    }

    private void sendEmailMessage(String eventId, EmailRequest request, MimeMessage message) {
        try {
            log.info("[Mail Service] ---> Sending Mail [Event Id: {}, to :{}]", eventId, request.to());
            javaMailSender.send(message);
        } catch (MailException ex) {
            log.warn("[Mail Service] ---> Error while sending mail [Event Id: {}]", eventId);
            throw ex;
        }
    }

    private void updateFailedNotificationDeliveryStatus(String eventId, Throwable exception) {
        String errorMessage = exception.getMessage();

        notificationDeliveryRepository.findByRequestEventId(eventId).ifPresentOrElse(
                notificationDelivery -> this.updateDlqNotificationDelivery(notificationDelivery, errorMessage),
                () -> log.warn("[Mail Service] ---> Notification delivery not found for failed email. eventId={}", eventId)
        );
    }

    private void updateSentNotificationDeliveryStatus(String eventId) {
        notificationDeliveryRepository.findByRequestEventId(eventId).ifPresentOrElse(
                this::updateSentNotificationDelivery,
                () -> log.warn("[Mail Service] ---> Notification delivery not found for sent email. eventId={}", eventId)
        );
    }

    private void updateDlqNotificationDelivery(TNotificationDelivery notificationDelivery, String errorMessage) {
        notificationDelivery.setStatus(NotificationDeliveryStatus.DLQ);
        notificationDelivery.setErrorMessage(errorMessage);
        int currentRetryCount = notificationDelivery.getRetryCount() == null ? 0 : notificationDelivery.getRetryCount();
        notificationDelivery.setRetryCount(currentRetryCount + retryConfig.getMaxAttempts());
        notificationDeliveryRepository.save(notificationDelivery);
    }

    private void updateSentNotificationDelivery(TNotificationDelivery notificationDelivery) {
        notificationDelivery.setStatus(NotificationDeliveryStatus.SENT);
        notificationDelivery.setErrorMessage(null);
        notificationDelivery.setSentAt(OffsetDateTime.now());
        notificationDeliveryRepository.save(notificationDelivery);
    }
}
