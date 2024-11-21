package io.hhplus.concert.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
            .title("Concert Reservation System API DOCS")
            .version("v1")
            .description("콘서트 예약 시스템 api 명세서");

        String queueTokenScheme = "queueToken";
        Components components = new Components()
            .addSecuritySchemes(queueTokenScheme, new SecurityScheme()
                .name(queueTokenScheme)
                .type(Type.APIKEY)
                .in(In.HEADER)
                .name("X-Queue-Token"));

        return new OpenAPI()
            .components(components)
            .info(info);
    }
}
