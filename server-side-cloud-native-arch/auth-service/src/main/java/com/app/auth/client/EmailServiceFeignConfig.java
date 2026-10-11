package com.app.auth.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class EmailServiceFeignConfig {

    @Bean
    public RequestInterceptor emailInternalKeyInterceptor(
            @Value("${email.internal-key}") String internalKey) {
        return template -> template.header("X-Internal-Key", internalKey);
    }
}
