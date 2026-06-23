package com.gym.crm.core.config;

import com.gym.crm.core.logging.RequestLoggingFilter;
import com.gym.crm.core.logging.TransactionIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TransactionIdFilter> transactionIdFilter() {
        FilterRegistrationBean<TransactionIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TransactionIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("transactionIdFilter");

        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        registration.setName("requestLoggingFilter");

        return registration;
    }
}
