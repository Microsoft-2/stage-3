package com.microsoft2.bigdata;

import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.CrawlerService;
import com.microsoft2.bigdata.search.application.IndexerService;
import com.microsoft2.bigdata.search.application.SearchService;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.EventBus;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.api.GutendexAdapter;
import com.microsoft2.bigdata.search.infrastructure.datalake.FileSystemDatalake;
import com.microsoft2.bigdata.search.infrastructure.messaging.ActiveMQEventBus;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;

public class ActiveMQTest {
    public static void main(String[] args) {
        System.out.println("--- DISTRIBUTED SEARCH (WITH ACTIVEMQ) ---");

        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);
        DatalakeRepository datalakeRepo = new FileSystemDatalake("datalake_store");
        BookProvider gutendex = new GutendexAdapter();
        
        EventBus eventBus = new ActiveMQEventBus("tcp://localhost:61616");

        CrawlerService crawler = new CrawlerService(datalakeRepo, gutendex, eventBus);
        IndexerService indexer = new IndexerService(datalakeRepo, indexRepo);
        SearchService searchEngine = new SearchService(indexRepo);

        eventBus.subscribe("document.downloaded", docId -> {
            System.out.println("Event recieved Starting asynchronous indexing");
            indexer.indexDocument(docId);
        });

        String bookId = "1661"; 
        System.out.println("Launching Crawler");
        crawler.ingestContent(bookId);

        System.out.println("Waiting for the system to process the event");
        try { Thread.sleep(10000); } catch (InterruptedException e) {}

        System.out.println("--- SEARCHING ---");
        Set<String> result = searchEngine.search("elementary");
        System.out.println("Results: " + result);

        hz.shutdown();
        System.exit(0);
    }
}
