package com.microsoft2.bigdata.search.domain.ports;

public interface EventBus {
    // Publicar un mensaje en un topic
    void publish(String topic, String message);
    
    // Suscribirse a un topic y definir qué hacer cuando llegue un mensaje
    void subscribe(String topic, EventConsumer consumer);

    // Interface funcional para el callback
    interface EventConsumer {
        void consume(String message);
    }
}