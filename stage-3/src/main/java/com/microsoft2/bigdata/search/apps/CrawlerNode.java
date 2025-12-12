package com.microsoft2.bigdata.search.apps;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.CrawlerService;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.EventBus;
import com.microsoft2.bigdata.search.infrastructure.api.GutendexAdapter;
import com.microsoft2.bigdata.search.infrastructure.datalake.HazelcastDatalake;
import com.microsoft2.bigdata.search.infrastructure.messaging.ActiveMQEventBus;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastConfigFactory;

public class CrawlerNode {
    public static void main(String[] args) {
        System.out.println("INICIANDO CRAWLER (REAL DATA)...");

        // 1. Configuración del Cluster
        Config config = HazelcastConfigFactory.createConfig();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        String brokerUrl = System.getenv().getOrDefault("BROKER_URL", "tcp://localhost:61616");
        
        // 2. Datalake Distribuido (Obligatorio para que PC2 vea los archivos)
        DatalakeRepository datalake = new HazelcastDatalake(hz);
        
        // 3. Proveedor REAL (Gutendex)
        BookProvider provider = new GutendexAdapter(); 
        
        EventBus eventBus = new ActiveMQEventBus(brokerUrl);
        CrawlerService crawler = new CrawlerService(datalake, provider, eventBus);

        int startId = Integer.parseInt(System.getenv().getOrDefault("BOOK_START", "1"));
        int endId = Integer.parseInt(System.getenv().getOrDefault("BOOK_END", "10"));

        System.out.println("Rango asignado: IDs " + startId + " al " + endId);

        for (int i = startId; i <= endId; i++) {
            String bookId = String.valueOf(i);
            try {
                System.out.println("Procesando libro ID: " + bookId);
                crawler.ingestContent(bookId);
                
                // IMPORTANTE: Pausa de 3-5 segundos para evitar bloqueo de IP por Gutendex
                Thread.sleep(3000); 
            } catch (Exception e) {
                System.err.println("Saltando libro " + bookId);
            }
        }
        
        System.out.println("Crawler finalizó su rango.");
    }
}
