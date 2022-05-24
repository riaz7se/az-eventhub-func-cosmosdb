package com.jnj.auditlog.common.config;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
@ConditionalOnAnyProperty(prefix = "auditlog.springdoc", name = "enable", havingValue = "true")
public class AlOpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI().info(new Info()
                .title("AuditLog Event Stream API")
                .version(appVersion)
                .description("AuditLog Event Stream API created using springdocs - " +
                        "a library for OpenAPI 3 with spring boot.")
                .termsOfService("http://swagger.io/terms/")
                .license(new License().name("Apache 2.0")
                        .url("http://springdoc.org")
                ));
    }
}
