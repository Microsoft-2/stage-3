package com.microsoft2.bigdata;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.*;
import com.microsoft2.bigdata.search.domain.ports.*;
import com.microsoft2.bigdata.search.infrastructure.api.GutendexAdapter;
import com.microsoft2.bigdata.search.infrastructure.messaging.ActiveMQEventBus; // <--- Nuevo
import com.microsoft2.bigdata.search.infrastructure.persistence.*;
import com.microsoft2.bigdata.search.infrastructure.datalake.*;


import java.util.Set;

public class GutendexTest {
    public static void main(String[] args) {
        System.out.println("--- BUSCADOR DISTRIBUIDO (CON ACTIVEMQ) ---");

        // 1. INFRAESTRUCTURA
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);
        DatalakeRepository datalakeRepo = new FileSystemDatalake("datalake_store");
        BookProvider gutendex = new GutendexAdapter();
        
        // Conectamos a ActiveMQ (que está corriendo en Docker)
        EventBus eventBus = new ActiveMQEventBus("tcp://localhost:61616");

        // 2. APLICACIÓN
        CrawlerService crawler = new CrawlerService(datalakeRepo, gutendex, eventBus);
        IndexerService indexer = new IndexerService(datalakeRepo, indexRepo);
        SearchService searchEngine = new SearchService(indexRepo);

        // 3. WIRING (Cableado): Configurar la reacción
        // Suscribimos al Indexer al topic "document.downloaded"
        // Cuando llegue un mensaje (id), ejecutamos indexer.indexDocument(id)
        eventBus.subscribe("document.downloaded", docId -> {
            System.out.println("⚡ Evento recibido! Iniciando indexación asíncrona...");
            indexer.indexDocument(docId);
        });

        // 4. EJECUCIÓN
        // Crawler descarga y publica evento. NO llamamos a indexer manualmente.
        String bookId = "1661"; 
        System.out.println("🚀 Lanzando Crawler...");
        crawler.ingestContent(bookId);

        // Como es asíncrono, necesitamos esperar un poco antes de buscar
        // En un sistema real no haríamos sleep, pero esto es un test de consola.
        System.out.println("⏳ Esperando a que el sistema procese el evento...");
        try { Thread.sleep(10000); } catch (InterruptedException e) {} // Esperar 10 segs

        // 5. BÚSQUEDA
        System.out.println("--- BUSCANDO ---");
        Set<String> result = searchEngine.search("elementary");
        System.out.println("Resultados: " + result);

        // Mantener vivo para ver logs si fuera necesario, o cerrar.
        hz.shutdown();
        System.exit(0);
    }
}