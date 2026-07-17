package com.gym.crm.automation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.automation.config.TestProperties;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Duration;
import java.util.Optional;

public class DeadLetterQueueClient {

    private static final String DLQ_NAME = "ActiveMQ.DLQ";
    private static final long POLL_MILLIS = 500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<JsonNode> awaitMessageForTrainer(String trainerUsername, Duration timeout) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(TestProperties.brokerUrl());

        try (Connection connection = connectionFactory.createConnection(TestProperties.brokerUser(), TestProperties.brokerPassword())) {
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                 MessageConsumer consumer = session.createConsumer(session.createQueue(DLQ_NAME))) {
                return pollForTrainer(consumer, trainerUsername, timeout);
            }
        } catch (JMSException e) {
            throw new IllegalStateException("Failed to read from " + DLQ_NAME, e);
        }
    }

    private Optional<JsonNode> pollForTrainer(MessageConsumer consumer, String trainerUsername, Duration timeout) throws JMSException {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadlineNanos) {
            long remainingMillis = Math.max(1, (deadlineNanos - System.nanoTime()) / 1_000_000);
            Message message = consumer.receive(Math.min(POLL_MILLIS, remainingMillis));

            Optional<JsonNode> match = toJson(message).filter(json -> matchesTrainer(json, trainerUsername));
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    private Optional<JsonNode> toJson(Message message) {
        if (!(message instanceof TextMessage textMessage)) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readTree(textMessage.getText()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean matchesTrainer(JsonNode json, String trainerUsername) {
        return trainerUsername.equals(json.path("originalMessage").path("trainerUsername").asText());
    }
}
