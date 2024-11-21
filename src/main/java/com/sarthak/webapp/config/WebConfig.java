package com.sarthak.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserVerificationInterceptor verificationInterceptor;

    public WebConfig(UserVerificationInterceptor verificationInterceptor) {
        this.verificationInterceptor = verificationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verificationInterceptor);
    }
}