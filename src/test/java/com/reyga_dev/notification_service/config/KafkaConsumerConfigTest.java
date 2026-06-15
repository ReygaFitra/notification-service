package com.reyga_dev.notification_service.config;

import com.reyga_dev.notification_service.config.interceptor.KafkaMdcNotificationRecordInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class KafkaConsumerConfigTest {

    @Test
    void should_ReturnKafkaListenerContainerFactory_When_ConfigurationIsValid() {
        // given
        KafkaProperties kafkaProperties = new KafkaProperties();
        CommonErrorHandler errorHandler = mock(CommonErrorHandler.class);
        KafkaMdcNotificationRecordInterceptor recordInterceptor = mock(KafkaMdcNotificationRecordInterceptor.class);
        KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig();

        // when
        ConcurrentKafkaListenerContainerFactory<String, String> result =
                kafkaConsumerConfig.notificationKafkaListenerContainerFactory(
                        kafkaProperties,
                        errorHandler,
                        recordInterceptor
                );

        // then
        assertNotNull(result);
        assertNotNull(result.getConsumerFactory());
        assertEquals(ContainerProperties.AckMode.MANUAL, result.getContainerProperties().getAckMode());
        assertSame(recordInterceptor, result.getRecordInterceptor());
        assertSame(errorHandler, ReflectionTestUtils.getField(result, "commonErrorHandler"));
    }
}
