package com.reyga_dev.notification_service.config;

import com.reyga_dev.notification_service.domain.exception.InvalidNotificationEventException;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class KafkaErrorHandlerConfigTest {

    @Test
    void should_ReturnDefaultErrorHandler_When_ConfigurationIsValid() {
        // given
        KafkaOperations<String, String> kafkaOperations = mockKafkaOperations();
        KafkaErrorHandlerConfig kafkaErrorHandlerConfig = new KafkaErrorHandlerConfig(kafkaOperations);

        // when
        DefaultErrorHandler result = kafkaErrorHandlerConfig.notificationDefaultErrorHandler();

        // then
        assertNotNull(result);
        assertEquals(Boolean.FALSE, result.removeClassification(IllegalArgumentException.class));
        assertEquals(Boolean.FALSE, result.removeClassification(InvalidNotificationEventException.class));
        verify(kafkaOperations, atLeastOnce()).isTransactional();
        verifyNoMoreInteractions(kafkaOperations);
    }

    @SuppressWarnings("unchecked")
    private KafkaOperations<String, String> mockKafkaOperations() {
        return mock(KafkaOperations.class);
    }
}
