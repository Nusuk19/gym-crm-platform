package com.gym.crm.automation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.automation.config.TestProperties;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Map;

public class WorkloadQueuePublisher {

    private static final String QUEUE_NAME = "trainer-workload-queue";
    private static final String MESSAGE_TYPE = "com.gym.crm.workload.messaging.TrainerWorkloadMessage";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publish(Map<String, Object> workloadMessage) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(TestProperties.brokerUrl());

        try (Connection connection = connectionFactory.createConnection(TestProperties.brokerUser(), TestProperties.brokerPassword())) {
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                Queue queue = session.createQueue(QUEUE_NAME);
                try (MessageProducer producer = session.createProducer(queue)) {
                    var message = session.createTextMessage(toJson(workloadMessage));
                    message.setStringProperty("_type", MESSAGE_TYPE);
                    producer.send(message);
                }
            }
        } catch (JMSException e) {
            throw new IllegalStateException(String.format("Failed to publish workload message to %s", QUEUE_NAME), e);
        }
    }

    private String toJson(Map<String, Object> workloadMessage) {
        try {
            return objectMapper.writeValueAsString(workloadMessage);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize workload message", e);
        }
    }
}