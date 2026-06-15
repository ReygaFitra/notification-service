package com.reyga_dev.notification_service;

import com.reyga_dev.notification_service.config.properties.EmailConfigProperties;
import com.reyga_dev.notification_service.config.properties.ResilienceConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(
		value = { EmailConfigProperties.class, ResilienceConfigProperties.class}
)
public class NotificationServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
