package com.reyga_dev.notification_service.config;

import com.reyga_dev.notification_service.domain.exception.InvalidNotificationEventException;
import com.reyga_dev.notification_service.domain.exception.NotificationProcessingException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler notificationDefaultErrorHandler(KafkaOperations<String, String> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (consumerRecord, exception) -> new TopicPartition(
                        consumerRecord.topic() + ".dlt",
                        consumerRecord.partition()
                )
        );

        FixedBackOff fixedBackOff = new FixedBackOff(1_000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer, fixedBackOff
        );

        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                InvalidNotificationEventException.class,
                NotificationProcessingException.class
        );

        return errorHandler;
    }

}
