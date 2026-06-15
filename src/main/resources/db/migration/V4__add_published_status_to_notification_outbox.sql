ALTER TABLE NOTIFICATION.T_NOTIFICATION_OUTBOX
    DROP CONSTRAINT IF EXISTS ck_notification_outbox_status;

ALTER TABLE NOTIFICATION.T_NOTIFICATION_OUTBOX
    ADD CONSTRAINT ck_notification_outbox_status
        CHECK (STATUS IN (
            'PENDING',
            'PUBLISHED',
            'SUCCESS',
            'RETRY',
            'FAILED'
        ));
