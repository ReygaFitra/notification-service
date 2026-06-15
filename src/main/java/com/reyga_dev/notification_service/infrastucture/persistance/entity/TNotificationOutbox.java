package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import com.reyga_dev.notification_service.domain.enums.NotificationOutboxStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "t_notification_outbox",
        schema = "notification",
        indexes = {
                @Index(name = "idx_notification_outbox_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_notification_outbox_event_id", columnList = "event_id")
        }
)
public class TNotificationOutbox extends BaseVersionableEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "request_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_outbox_request")
    )
    private TNotificationRequest request;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "message_key", nullable = false, length = 100)
    private String messageKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NotificationOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (status == null) {
            status = NotificationOutboxStatus.PENDING;
        }

        if (retryCount == null) {
            retryCount = 0;
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

    public TNotificationOutbox() {
    }

    public TNotificationOutbox(
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            String createdBy,
            String modifiedBy,
            UUID id,
            TNotificationRequest request,
            String eventId,
            String topicName,
            String messageKey,
            String payload,
            NotificationOutboxStatus status,
            Integer retryCount,
            String errorMessage,
            OffsetDateTime publishedAt
    ) {
        super(createdAt, updatedAt, createdBy, modifiedBy);
        this.id = id;
        this.request = request;
        this.eventId = eventId;
        this.topicName = topicName;
        this.messageKey = messageKey;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.publishedAt = publishedAt;
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

    public TNotificationRequest getRequest() {
        return request;
    }

    public void setRequest(TNotificationRequest request) {
        this.request = request;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public NotificationOutboxStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationOutboxStatus status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public static final class Builder {
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private String createdBy;
        private String modifiedBy;
        private Integer version;
        private UUID id;
        private TNotificationRequest request;
        private String eventId;
        private String topicName;
        private String messageKey;
        private String payload;
        private NotificationOutboxStatus status;
        private Integer retryCount;
        private String errorMessage;
        private OffsetDateTime publishedAt;

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

        public Builder request(TNotificationRequest request) {
            this.request = request;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder messageKey(String messageKey) {
            this.messageKey = messageKey;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder status(NotificationOutboxStatus status) {
            this.status = status;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder publishedAt(OffsetDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public TNotificationOutbox build() {
            TNotificationOutbox outbox = new TNotificationOutbox(
                    createdAt,
                    updatedAt,
                    createdBy,
                    modifiedBy,
                    id,
                    request,
                    eventId,
                    topicName,
                    messageKey,
                    payload,
                    status,
                    retryCount,
                    errorMessage,
                    publishedAt
            );
            outbox.version = version;
            return outbox;
        }
    }
}
