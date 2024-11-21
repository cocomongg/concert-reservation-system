package io.hhplus.concert.config.web;

import io.hhplus.concert.config.web.interceptor.WaitingQueueTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final WaitingQueueTokenInterceptor waitingQueueTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(waitingQueueTokenInterceptor)
            .addPathPatterns("/api/v1/concerts/**",
                "/api/v1/members/*/points",
                "/api/v1/payments/*");
    }
}
