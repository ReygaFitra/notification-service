package com.reyga_dev.notification_service.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceUtilsTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ServiceUtils serviceUtils;

    @Test
    void should_ReturnParsedObject_When_JsonIsValid() throws JacksonException {
        // given
        String json = "{\"value\":\"test\"}";
        TestPayload expectedPayload = new TestPayload("test");
        when(objectMapper.readValue(json, TestPayload.class)).thenReturn(expectedPayload);

        // when
        TestPayload result = serviceUtils.readValue(json, TestPayload.class);

        // then
        assertSame(expectedPayload, result);
        verify(objectMapper).readValue(json, TestPayload.class);
        verifyNoMoreInteractions(objectMapper);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_JsonIsInvalid() throws JacksonException {
        // given
        String json = "{invalid-json}";
        JacksonException jacksonException = new JacksonException("Invalid JSON") {
        };
        when(objectMapper.readValue(json, TestPayload.class)).thenThrow(jacksonException);

        // when
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> serviceUtils.readValue(json, TestPayload.class)
        );

        // then
        assertEquals("Invalid JSON message", result.getMessage());
        assertSame(jacksonException, result.getCause());
        verify(objectMapper).readValue(json, TestPayload.class);
        verifyNoMoreInteractions(objectMapper);
    }

    private record TestPayload(String value) {
    }
}
