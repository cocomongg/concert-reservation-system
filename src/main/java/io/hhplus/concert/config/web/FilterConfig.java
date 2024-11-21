package io.hhplus.concert.config.web;

import io.hhplus.concert.config.web.filter.ApiLoggingFilter;
import io.hhplus.concert.config.web.filter.WaitingQueueTokenFilter;
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
        filterRegistrationBean.addUrlPatterns("/api/v1/queues/tokens/order-info", "/api/v1/concerts/**",
            "/api/v1/members/*", "/api/v1/payments/*");

        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilterRegistration() {
        FilterRegistrationBean<ApiLoggingFilter> filterRegistrationBean =
            new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new ApiLoggingFilter());
        filterRegistrationBean.addUrlPatterns("/api/*");

        return filterRegistrationBean;
    }
}
