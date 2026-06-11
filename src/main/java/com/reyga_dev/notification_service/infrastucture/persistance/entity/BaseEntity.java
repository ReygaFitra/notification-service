package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_at", nullable = false)
    protected OffsetDateTime createdAt;

    @Column(name = "updated_at")
    protected OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 50)
    protected String createdBy;

    @Column(name = "modified_by", length = 50)
    protected String modifiedBy;

    protected OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    protected BaseEntity(OffsetDateTime createdAt, OffsetDateTime updatedAt, String createdBy, String modifiedBy) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    protected BaseEntity() {
    }

    protected void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    protected OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    protected void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    protected String getCreatedBy() {
        return createdBy;
    }

    protected void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    protected String getModifiedBy() {
        return modifiedBy;
    }

    protected void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
