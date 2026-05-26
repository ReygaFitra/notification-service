package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;

public abstract class BaseVersionableEntity extends BaseEntity {
    @Version
    @Column(name = "version", nullable = false)
    protected Integer version;

    protected BaseVersionableEntity(OffsetDateTime createdAt, OffsetDateTime updatedAt, String createdBy, String modifiedBy) {
        super(createdAt, updatedAt, createdBy, modifiedBy);
    }

    protected BaseVersionableEntity() {
    }

    protected Integer getVersion() {
        return version;
    }

    protected void setVersion(Integer version) {
        this.version = version;
    }
}
