package com.reyga_dev.notification_service.common;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ServiceUtils {

    private final ObjectMapper objectMapper;

    public ServiceUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T readValue(String json, Class<T> targetClass) {
        return objectMapper.readValue(json, targetClass);
    }

    public <T> T convertValue(Object value, Class<T> targetClass) {
        return objectMapper.convertValue(value, targetClass);
    }

}
