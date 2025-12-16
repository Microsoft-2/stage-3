package com.microsoft2.bigdata.search.domain.ports;

public interface EventBus {
    void publish(String topic, String message);
    
    void subscribe(String topic, EventConsumer consumer);

    interface EventConsumer {
        void consume(String message);
    }
}
