package com.reyga_dev.notification_service.common;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ServiceUtils {

    private final ObjectMapper objectMapper;

    public ServiceUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T readValue(String json, Class<T> targetClass) {
        try {
            return objectMapper.readValue(json, targetClass);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Invalid JSON message", ex);
        }
    }

}
