package com.reyga_dev.notification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationRequestedTopic() {
        return TopicBuilder.name("notification.requested")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationRequestedDltTopic() {
        return TopicBuilder.name("notification.requested.dlt")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
