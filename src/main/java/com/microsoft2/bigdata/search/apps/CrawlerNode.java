package com.microsoft2.bigdata.search.apps;

import com.microsoft2.bigdata.search.application.CrawlerService;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.EventBus;
import com.microsoft2.bigdata.search.infrastructure.api.GutendexAdapter;
import com.microsoft2.bigdata.search.infrastructure.datalake.FileSystemDatalake;
import com.microsoft2.bigdata.search.infrastructure.messaging.ActiveMQEventBus;

public class CrawlerNode {
    public static void main(String[] args) {
        System.out.println("🕷️ INICIANDO CRAWLER MASIVO...");

        String brokerUrl = System.getenv().getOrDefault("BROKER_URL", "tcp://localhost:61616");
        String datalakePath = System.getenv().getOrDefault("DATALAKE_PATH", "datalake_store");
        
        // Configuración del rango de libros (Por defecto descarga del ID 1 al 10)
        int startId = Integer.parseInt(System.getenv().getOrDefault("BOOK_START", "1"));
        int endId = Integer.parseInt(System.getenv().getOrDefault("BOOK_END", "10"));

        DatalakeRepository datalake = new FileSystemDatalake(datalakePath);
        BookProvider provider = new GutendexAdapter();
        EventBus eventBus = new ActiveMQEventBus(brokerUrl);
        CrawlerService crawler = new CrawlerService(datalake, provider, eventBus);

        System.out.println("📚 Rango asignado: IDs " + startId + " al " + endId);

        for (int i = startId; i <= endId; i++) {
            String bookId = String.valueOf(i);
            try {
                System.out.println("🕷️ Procesando libro ID: " + bookId);
                crawler.ingestContent(bookId);
                
                // Pausa pequeña para no ser baneados por Gutendex (Rate Limiting)
                Thread.sleep(2000); 
            } catch (Exception e) {
                System.err.println("⚠️ Error procesando libro " + bookId + ": " + e.getMessage());
            }
        }
        
        System.out.println("✅ Crawler finalizó su rango asignado.");
    }
}