package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import com.reyga_dev.notification_service.domain.enums.NotificationChannel;
import com.reyga_dev.notification_service.domain.enums.NotificationRequestStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "t_notification_request",
        schema = "notification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_request_event_id",
                        columnNames = "event_id"
                )
        }
)
public class TNotificationRequest extends BaseVersionableEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "recipient_id", nullable = false, length = 100)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "topic_name", length = 100)
    private String topicName;

    @Column(name = "partition_id")
    private Integer partitionId;

    @Column(name = "offset_id")
    private Long offsetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private NotificationRequestStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (status == null) {
            status = NotificationRequestStatus.PROCESSING;
        }

        if (createdBy == null) {
            createdBy = "SYSTEM";
        }

        if (version == null) {
            version = 1;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();

        if (modifiedBy == null) {
            modifiedBy = "SYSTEM";
        }
    }

    public TNotificationRequest() {
    }

    public TNotificationRequest(
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            String createdBy,
            String modifiedBy,
            UUID id,
            String eventId,
            String eventType,
            String recipientId,
            NotificationChannel channel,
            Map<String, Object> payload,
            String topicName,
            Integer partitionId,
            Long offsetId,
            NotificationRequestStatus status,
            String errorMessage
    ) {
        super(createdAt, updatedAt, createdBy, modifiedBy);
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.recipientId = recipientId;
        this.channel = channel;
        this.payload = payload;
        this.topicName = topicName;
        this.partitionId = partitionId;
        this.offsetId = offsetId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public TNotificationRequest(
            UUID id,
            String eventId,
            String eventType,
            String recipientId,
            NotificationChannel channel,
            Map<String, Object> payload,
            String topicName,
            Integer partitionId,
            Long offsetId,
            NotificationRequestStatus status,
            String errorMessage
    ) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.recipientId = recipientId;
        this.channel = channel;
        this.payload = payload;
        this.topicName = topicName;
        this.partitionId = partitionId;
        this.offsetId = offsetId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    public Long getOffsetId() {
        return offsetId;
    }

    public void setOffsetId(Long offsetId) {
        this.offsetId = offsetId;
    }

    public NotificationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationRequestStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static final class Builder {
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private String createdBy;
        private String modifiedBy;
        private Integer version;
        private UUID id;
        private String eventId;
        private String eventType;
        private String recipientId;
        private NotificationChannel channel;
        private Map<String, Object> payload;
        private String topicName;
        private Integer partitionId;
        private Long offsetId;
        private NotificationRequestStatus status;
        private String errorMessage;

        private Builder() {
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder modifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public Builder version(Integer version) {
            this.version = version;
            return this;
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder recipientId(String recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder partitionId(Integer partitionId) {
            this.partitionId = partitionId;
            return this;
        }

        public Builder offsetId(Long offsetId) {
            this.offsetId = offsetId;
            return this;
        }

        public Builder status(NotificationRequestStatus status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public TNotificationRequest build() {
            TNotificationRequest request = new TNotificationRequest(
                    createdAt,
                    updatedAt,
                    createdBy,
                    modifiedBy,
                    id,
                    eventId,
                    eventType,
                    recipientId,
                    channel,
                    payload,
                    topicName,
                    partitionId,
                    offsetId,
                    status,
                    errorMessage
            );
            request.version = version;
            return request;
        }
    }
}
