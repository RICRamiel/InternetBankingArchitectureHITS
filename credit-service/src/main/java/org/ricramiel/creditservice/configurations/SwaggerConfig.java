package org.ricramiel.creditservice.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Car service API")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes("user-id-header",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(CustomHeaders.USER_ID_HEADER)
                        )
                        .addSecuritySchemes("user-role-header",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(CustomHeaders.USER_ROLES_HEADER)
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .addSecurityItem(new SecurityRequirement().addList("user-id-header"))
                .addSecurityItem(new SecurityRequirement().addList("user-role-header"));
    }
}