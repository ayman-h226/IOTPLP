package com.plp.iotplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // Activer les tâches planifiées
@OpenAPIDefinition(info = @Info(title = "IoT Platform API", version = "v1", description = "APIs for managing IoT Hubs and Sensors"))
@SpringBootApplication
public class IotplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(IotplatformApplication.class, args);
	}

}
