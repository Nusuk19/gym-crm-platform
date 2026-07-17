package com.gym.crm.workload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.workload.exception.InvalidMessageException;
import com.gym.crm.workload.messaging.TrainerWorkloadMessage;
import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.ErrorHandler;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableJms
public class JmsConfig {

    @Value("${app.jms.consumer.concurrency}")
    private String concurrency;

    @Bean
    @Primary
    public ActiveMQConnectionFactory activeMQConnectionFactory(@Value("${spring.activemq.broker-url}") String brokerUrl,
                                                               @Value("${spring.activemq.user}") String user,
                                                               @Value("${spring.activemq.password}") String password,
                                                               @Value("${app.jms.redelivery.maximum-redeliveries}") int maxRedeliveries,
                                                               @Value("${app.jms.redelivery.initial-delay-ms}") long initialDelay,
                                                               @Value("${app.jms.redelivery.delay-ms}") long delay) {
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setMaximumRedeliveries(maxRedeliveries);
        policy.setInitialRedeliveryDelay(initialDelay);
        policy.setRedeliveryDelay(delay);

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, password, brokerUrl);
        factory.setTrustedPackages(List.of(
                "com.gym.crm.workload.messaging",
                "com.gym.crm.workload.openapi",
                "java.lang",
                "java.util",
                "java.time"));
        factory.setTrustAllPackages(false);
        factory.setRedeliveryPolicy(policy);

        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                          DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSessionTransacted(true);
        factory.setErrorHandler(jmsErrorHandler());
        factory.setConcurrency(concurrency);

        return factory;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of("trainerWorkloadMessage", TrainerWorkloadMessage.class));
        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Bean
    public ErrorHandler jmsErrorHandler() {
        return throwable -> {
            Throwable cause = throwable.getCause();

            if (cause instanceof InvalidMessageException) {
                log.warn("Invalid workload message moved to DLQ: {}", cause.getMessage());
            } else {
                log.error("Workload message processing failed after retries, moved to DLQ", throwable);
            }
        };
    }
}