package com.microsoft2.bigdata.search.apps;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.IndexerService;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.EventBus;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.datalake.FileSystemDatalake;
import com.microsoft2.bigdata.search.infrastructure.messaging.ActiveMQEventBus;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastConfigFactory;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;
 
public class IndexerNode {
    public static void main(String[] args) {
        System.out.println("STARTING INDEXER NODE...");

        String brokerUrl = System.getenv().getOrDefault("BROKER_URL", "tcp://localhost:61616");
        String datalakePath = System.getenv().getOrDefault("DATALAKE_PATH", "datalake_store");

        // 1. Infraestructura
        // IMPORTANTE: Hazelcast se unirá automáticamente al clúster si hay otros nodos
        Config config = HazelcastConfigFactory.createConfig();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config); 
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);
        DatalakeRepository datalake = new FileSystemDatalake(datalakePath);
        EventBus eventBus = new ActiveMQEventBus(brokerUrl);

        // 2. Aplicación
        IndexerService indexer = new IndexerService(datalake, indexRepo);

        // 3. Suscripción
        System.out.println("Indexer: Waiting for events on 'document.downloaded'...");
        eventBus.subscribe("document.downloaded", docId -> {
            System.out.println("Event received. Indexing: " + docId);
            indexer.indexDocument(docId);
        });
        
        // Mantener vivo el programa (Hazelcast ya lo hace, pero por seguridad)
        synchronized (IndexerNode.class) {
            try { IndexerNode.class.wait(); } catch (InterruptedException e) {}
        }
    }
}