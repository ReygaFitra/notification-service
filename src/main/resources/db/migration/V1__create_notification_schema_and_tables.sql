CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS NOTIFICATION;

CREATE TABLE NOTIFICATION.T_NOTIFICATION_REQUEST (
    ID                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    EVENT_ID            VARCHAR(100) NOT NULL,
    EVENT_TYPE          VARCHAR(50) NOT NULL,

    RECIPIENT_ID        VARCHAR(100) NOT NULL,
    CHANNEL             VARCHAR(20) NOT NULL,

    PAYLOAD             JSONB NOT NULL,
    TOPIC_NAME          VARCHAR(100),
    PARTITION_ID        INT4,
    OFFSET_ID           INT8,

    STATUS              VARCHAR(50) NOT NULL DEFAULT 'PROCESSING',
    ERROR_MESSAGE       TEXT,

    CREATED_AT          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UPDATED_AT          TIMESTAMPTZ,
    CREATED_BY          VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    MODIFIED_BY         VARCHAR(50),
    VERSION             INT2 NOT NULL DEFAULT 1,

    CONSTRAINT uk_notification_request_event_id UNIQUE (EVENT_ID),

    CONSTRAINT ck_notification_request_channel
        CHECK (CHANNEL IN ('EMAIL', 'SMS', 'WHATSAPP', 'PUSH')),

    CONSTRAINT ck_notification_request_status
        CHECK (STATUS IN (
            'RECEIVED',
            'PROCESSING',
            'FAILED',
            'DLQ'
        ))
);

CREATE TABLE NOTIFICATION.T_NOTIFICATION_DELIVERY (
    ID                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    REQUEST_ID          UUID NOT NULL,
    PROVIDER            VARCHAR(50) NOT NULL,
    RECIPIENT_ADDRESS   VARCHAR(255) NOT NULL,

    STATUS              VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    RETRY_COUNT         INT2 NOT NULL DEFAULT 0,
    ERROR_MESSAGE       TEXT,

    SENT_AT             TIMESTAMPTZ,

    CREATED_AT          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UPDATED_AT          TIMESTAMPTZ,
    CREATED_BY          VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    MODIFIED_BY         VARCHAR(50),
    VERSION             INT2 NOT NULL DEFAULT 1,

    CONSTRAINT fk_notification_delivery_request
        FOREIGN KEY (REQUEST_ID)
        REFERENCES NOTIFICATION.T_NOTIFICATION_REQUEST (ID)
        ON DELETE CASCADE,

    CONSTRAINT ck_notification_delivery_status
        CHECK (status IN (
        'PENDING',
        'SENDING',
        'SENT',
        'FAILED',
        'RETRYING',
        'DLQ'
        )),

    CONSTRAINT ck_notification_delivery_retry_count
        CHECK (retry_count >= 0)
);