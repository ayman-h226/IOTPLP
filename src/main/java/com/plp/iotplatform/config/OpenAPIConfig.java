package com.plp.iotplatform.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IoT Platform API")
                        .version("1.0")
                        .description("API documentation for IoT Platform")
                        .contact(new Contact()
                                .name("IoT Platform Team")
                                .email("contact@iotplatform.com")));
    }
}