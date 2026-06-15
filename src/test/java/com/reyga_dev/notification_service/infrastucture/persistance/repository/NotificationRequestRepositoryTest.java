package com.reyga_dev.notification_service.infrastucture.persistance.repository;

import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class NotificationRequestRepositoryTest {

    private final NotificationRequestRepository notificationRequestRepository = mock(NotificationRequestRepository.class);

    @Test
    void should_ReturnTrue_When_EventIdExists() {
        // given
        String eventId = "event-001";
        when(notificationRequestRepository.existsByEventId(eventId)).thenReturn(true);

        // when
        boolean result = notificationRequestRepository.existsByEventId(eventId);

        // then
        assertTrue(result);
        verify(notificationRequestRepository).existsByEventId(eventId);
        verifyNoMoreInteractions(notificationRequestRepository);
    }

    @Test
    void should_ReturnFalse_When_EventIdDoesNotExist() {
        // given
        String eventId = "event-001";
        when(notificationRequestRepository.existsByEventId(eventId)).thenReturn(false);

        // when
        boolean result = notificationRequestRepository.existsByEventId(eventId);

        // then
        assertFalse(result);
        verify(notificationRequestRepository).existsByEventId(eventId);
        verifyNoMoreInteractions(notificationRequestRepository);
    }

    @Test
    void should_ReturnNotificationRequest_When_EventIdIsFound() {
        // given
        String eventId = "event-001";
        TNotificationRequest request = new TNotificationRequest();
        when(notificationRequestRepository.findByEventId(eventId)).thenReturn(Optional.of(request));

        // when
        Optional<TNotificationRequest> result = notificationRequestRepository.findByEventId(eventId);

        // then
        assertTrue(result.isPresent());
        assertSame(request, result.orElseThrow());
        verify(notificationRequestRepository).findByEventId(eventId);
        verifyNoMoreInteractions(notificationRequestRepository);
    }

    @Test
    void should_ReturnEmptyOptional_When_EventIdIsNotFound() {
        // given
        String eventId = "event-001";
        when(notificationRequestRepository.findByEventId(eventId)).thenReturn(Optional.empty());

        // when
        Optional<TNotificationRequest> result = notificationRequestRepository.findByEventId(eventId);

        // then
        assertTrue(result.isEmpty());
        verify(notificationRequestRepository).findByEventId(eventId);
        verifyNoMoreInteractions(notificationRequestRepository);
    }

    @Test
    void should_ReturnSavedNotificationRequest_When_SaveIsCalled() {
        // given
        TNotificationRequest request = new TNotificationRequest();
        when(notificationRequestRepository.save(request)).thenReturn(request);

        // when
        TNotificationRequest result = notificationRequestRepository.save(request);

        // then
        assertSame(request, result);
        verify(notificationRequestRepository).save(request);
        verifyNoMoreInteractions(notificationRequestRepository);
    }

    @Test
    void should_ReturnNotificationRequest_When_FindByIdIsFound() {
        // given
        UUID id = UUID.randomUUID();
        TNotificationRequest request = new TNotificationRequest();
        when(notificationRequestRepository.findById(id)).thenReturn(Optional.of(request));

        // when
        Optional<TNotificationRequest> result = notificationRequestRepository.findById(id);

        // then
        assertTrue(result.isPresent());
        assertSame(request, result.orElseThrow());
        verify(notificationRequestRepository).findById(id);
        verifyNoMoreInteractions(notificationRequestRepository);
    }
}
