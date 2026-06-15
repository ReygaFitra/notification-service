package com.reyga_dev.notification_service.config.interceptor;

import com.reyga_dev.notification_service.domain.enums.ServiceHeaders;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class KafkaMdcNotificationRecordInterceptor implements RecordInterceptor<String, String> {

    @Override
    public @Nullable ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> eventRecord, Consumer<String, String> consumerData) {
        String requestId = extractRequestId(eventRecord);

        MDC.put("requestId", requestId);
        MDC.put("kafkaTopic", eventRecord.topic());
        MDC.put("kafkaPartition", String.valueOf(eventRecord.partition()));
        MDC.put("kafkaOffset", String.valueOf(eventRecord.offset()));

        return eventRecord;
    }

    @Override
    public void afterRecord(ConsumerRecord<String, String> eventRecord, Consumer<String, String> consumerData) {
        MDC.clear();
    }

    private String extractRequestId(ConsumerRecord<String, String> eventRecord) {
        Header header = eventRecord.headers().lastHeader(ServiceHeaders.REQUEST_ID.getLabel());

        if (header == null || header.value() == null) {
            return UUID.randomUUID().toString();
        }

        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
