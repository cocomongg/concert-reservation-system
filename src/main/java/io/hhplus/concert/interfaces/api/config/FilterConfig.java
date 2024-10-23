package io.hhplus.concert.interfaces.api.config;

import io.hhplus.concert.interfaces.api.config.filter.WaitingQueueTokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<WaitingQueueTokenFilter> waitingQueueTokenFilterRegistration() {
        FilterRegistrationBean<WaitingQueueTokenFilter> filterRegistrationBean =
            new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new WaitingQueueTokenFilter());
        filterRegistrationBean.addUrlPatterns("/api/v1/queues/tokens/order-info", "/api/v1/concerts/*",
            "/api/v1/members/*", "/api/v1/payments/*");

        return filterRegistrationBean;
    }
}
