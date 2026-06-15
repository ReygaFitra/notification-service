package com.reyga_dev.notification_service.infrastucture.persistance.repository;

import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationDelivery;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class NotificationDeliveryRepositoryTest {

    private final NotificationDeliveryRepository notificationDeliveryRepository = mock(NotificationDeliveryRepository.class);

    @Test
    void should_ReturnSavedNotificationDelivery_When_SaveIsCalled() {
        // given
        TNotificationDelivery delivery = new TNotificationDelivery();
        when(notificationDeliveryRepository.save(delivery)).thenReturn(delivery);

        // when
        TNotificationDelivery result = notificationDeliveryRepository.save(delivery);

        // then
        assertSame(delivery, result);
        verify(notificationDeliveryRepository).save(delivery);
        verifyNoMoreInteractions(notificationDeliveryRepository);
    }

    @Test
    void should_ReturnNotificationDelivery_When_FindByIdIsFound() {
        // given
        UUID id = UUID.randomUUID();
        TNotificationDelivery delivery = new TNotificationDelivery();
        when(notificationDeliveryRepository.findById(id)).thenReturn(Optional.of(delivery));

        // when
        Optional<TNotificationDelivery> result = notificationDeliveryRepository.findById(id);

        // then
        assertTrue(result.isPresent());
        assertSame(delivery, result.orElseThrow());
        verify(notificationDeliveryRepository).findById(id);
        verifyNoMoreInteractions(notificationDeliveryRepository);
    }

    @Test
    void should_ReturnEmptyOptional_When_FindByIdIsNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(notificationDeliveryRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Optional<TNotificationDelivery> result = notificationDeliveryRepository.findById(id);

        // then
        assertTrue(result.isEmpty());
        verify(notificationDeliveryRepository).findById(id);
        verifyNoMoreInteractions(notificationDeliveryRepository);
    }
}
