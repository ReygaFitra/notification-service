package com.reyga_dev.notification_service.config;

import com.reyga_dev.notification_service.domain.exception.InvalidNotificationEventException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    private final KafkaOperations<String, String> kafkaOperations;

    public KafkaErrorHandlerConfig(KafkaOperations<String, String> kafkaOperations) {
        this.kafkaOperations = kafkaOperations;
    }

    @Bean
    public DefaultErrorHandler notificationDefaultErrorHandler() {
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
                InvalidNotificationEventException.class
        );

        return errorHandler;
    }

}
