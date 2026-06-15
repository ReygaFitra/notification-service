package com.reyga_dev.notification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTopicConfigTest {

    private final KafkaTopicConfig kafkaTopicConfig = new KafkaTopicConfig();

    @Test
    void should_ReturnNotificationRequestedTopic_When_BeanIsCreated() {
        // given

        // when
        NewTopic result = kafkaTopicConfig.notificationRequestedTopic();

        // then
        assertEquals("notification.requested", result.name());
        assertEquals(3, result.numPartitions());
        assertEquals(1, result.replicationFactor());
    }

    @Test
    void should_ReturnNotificationRequestedDltTopic_When_BeanIsCreated() {
        // given

        // when
        NewTopic result = kafkaTopicConfig.notificationRequestedDltTopic();

        // then
        assertEquals("notification.requested.dlt", result.name());
        assertEquals(3, result.numPartitions());
        assertEquals(1, result.replicationFactor());
    }
}
