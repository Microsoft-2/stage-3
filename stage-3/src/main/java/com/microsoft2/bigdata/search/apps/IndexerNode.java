package com.microsoft2.bigdata.search.apps;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.IndexerService;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.EventBus;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.datalake.HazelcastDatalake;
import com.microsoft2.bigdata.search.infrastructure.messaging.ActiveMQEventBus;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastConfigFactory;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;
 
public class IndexerNode {
    public static void main(String[] args) {
        System.out.println("INICIANDO INDEXER NODE...");

        String brokerUrl = System.getenv().getOrDefault("BROKER_URL", "tcp://localhost:61616");

        // 1. Infraestructura
        Config config = HazelcastConfigFactory.createConfig();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config); 
        
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);
        
        // 2. Use Distributed Datalake (Allows reading data saved by PC1)
        DatalakeRepository datalake = new HazelcastDatalake(hz);
        
        EventBus eventBus = new ActiveMQEventBus(brokerUrl);

        // 3. Aplicación
        IndexerService indexer = new IndexerService(datalake, indexRepo);

        // 4. Suscripción
        System.out.println("Indexer: Esperando eventos en 'document.downloaded'...");
        eventBus.subscribe("document.downloaded", docId -> {
            System.out.println("Evento recibido. Indexando: " + docId);
            indexer.indexDocument(docId);
        });
        
        // Mantener vivo
        synchronized (IndexerNode.class) {
            try { IndexerNode.class.wait(); } catch (InterruptedException e) {}
        }
    }
}
