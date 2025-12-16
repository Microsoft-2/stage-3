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
        System.out.println("STARTING CRAWLER...");

        String brokerUrl = System.getenv().getOrDefault("BROKER_URL", "tcp://localhost:61616");
        String datalakePath = System.getenv().getOrDefault("DATALAKE_PATH", "datalake_store");
        
        int startId = Integer.parseInt(System.getenv().getOrDefault("BOOK_START", "1"));
        int endId = Integer.parseInt(System.getenv().getOrDefault("BOOK_END", "10"));

        DatalakeRepository datalake = new FileSystemDatalake(datalakePath);
        BookProvider provider = new GutendexAdapter();
        EventBus eventBus = new ActiveMQEventBus(brokerUrl);
        CrawlerService crawler = new CrawlerService(datalake, provider, eventBus);

        System.out.println("Range assigned: IDs " + startId + " to " + endId);

        for (int i = startId; i <= endId; i++) {
            String bookId = String.valueOf(i);
            try {
                System.out.println("Processing book ID: " + bookId);
                crawler.ingestContent(bookId);
                Thread.sleep(5000); 
            } catch (Exception e) {
                System.err.println("Error processing book " + bookId + ": " + e.getMessage());
            }
        }

        System.out.println("Crawler finished its assigned range.");
    }
}
