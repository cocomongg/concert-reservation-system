package io.hhplus.concert.interfaces.api.config;

import io.hhplus.concert.interfaces.api.config.interceptor.WaitingQueueTokenInterceptor;
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
            .addPathPatterns("/api/v1/queues/tokens/order-info",
                "/api/v1/concerts/*",
                "/api/v1/members/*/points",
                "/api/v1/payments/*");
    }
}
