package com.microsoft2.bigdata.search.infrastructure.messaging;

import com.microsoft2.bigdata.search.domain.ports.EventBus;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ActiveMQEventBus implements EventBus {
    private final Connection connection;
    private final Session session;

    public ActiveMQEventBus(String brokerUrl) {
        try {
            // Conectamos con el servidor de ActiveMQ
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection();
            this.connection.start();
            // false = sin transacciones, AUTO_ACKNOWLEDGE = confirmación automática
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new RuntimeException("Error conectando a ActiveMQ", e);
        }
    }

    @Override
    public void publish(String topicName, String messageContent) {
        try {
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
            System.out.println("📨 EventBus: Publicado en '" + topicName + "': " + messageContent);
        } catch (JMSException e) {
            throw new RuntimeException("Error publicando mensaje", e);
        }
    }

    @Override
    public void subscribe(String topicName, EventConsumer consumer) {
        try {
            Destination destination = session.createTopic(topicName);
            MessageConsumer jmsConsumer = session.createConsumer(destination);
            
            // Listener asíncrono: Se ejecuta en un hilo separado cuando llega un mensaje
            jmsConsumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String text = textMessage.getText();
                        System.out.println("📩 EventBus: Recibido en '" + topicName + "': " + text);
                        consumer.consume(text);
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException("Error suscribiendo", e);
        }
    }
}