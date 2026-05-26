package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import com.reyga_dev.notification_service.domain.enums.NotificationDeliveryStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "t_notification_delivery", schema = "notification")
public class TNotificationDelivery extends BaseVersionableEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "request_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_delivery_request")
    )
    private TNotificationRequest request;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "recipient_address", nullable = false, length = 255)
    private String recipientAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NotificationDeliveryStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (status == null) {
            status = NotificationDeliveryStatus.PENDING;
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

    public TNotificationDelivery() {
    }

    public TNotificationDelivery(
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            String createdBy,
            String modifiedBy,
            UUID id,
            TNotificationRequest request,
            String provider,
            String recipientAddress,
            NotificationDeliveryStatus status,
            Integer retryCount,
            String errorMessage,
            OffsetDateTime sentAt
    ) {
        super(createdAt, updatedAt, createdBy, modifiedBy);
        this.id = id;
        this.request = request;
        this.provider = provider;
        this.recipientAddress = recipientAddress;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
    }

    public TNotificationDelivery(
            UUID id,
            TNotificationRequest request,
            String provider,
            String recipientAddress,
            NotificationDeliveryStatus status,
            Integer retryCount,
            String errorMessage,
            OffsetDateTime sentAt
    ) {
        this.id = id;
        this.request = request;
        this.provider = provider;
        this.recipientAddress = recipientAddress;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public NotificationDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationDeliveryStatus status) {
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

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public static final class Builder {
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private String createdBy;
        private String modifiedBy;
        private Integer version;
        private UUID id;
        private TNotificationRequest request;
        private String provider;
        private String recipientAddress;
        private NotificationDeliveryStatus status;
        private Integer retryCount;
        private String errorMessage;
        private OffsetDateTime sentAt;

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

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder recipientAddress(String recipientAddress) {
            this.recipientAddress = recipientAddress;
            return this;
        }

        public Builder status(NotificationDeliveryStatus status) {
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

        public Builder sentAt(OffsetDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public TNotificationDelivery build() {
            TNotificationDelivery delivery = new TNotificationDelivery(
                    createdAt,
                    updatedAt,
                    createdBy,
                    modifiedBy,
                    id,
                    request,
                    provider,
                    recipientAddress,
                    status,
                    retryCount,
                    errorMessage,
                    sentAt
            );
            delivery.version = version;
            return delivery;
        }
    }
}
