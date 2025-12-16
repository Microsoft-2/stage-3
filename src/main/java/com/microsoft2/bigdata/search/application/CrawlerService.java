package com.microsoft2.bigdata.search.application;

import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;
import com.microsoft2.bigdata.search.domain.ports.EventBus;

public class CrawlerService {
    private final DatalakeRepository datalake;
    private final BookProvider bookProvider;
    private final EventBus eventBus;

    public CrawlerService(DatalakeRepository datalake, BookProvider bookProvider, EventBus eventBus){
        this.datalake = datalake;
        this.bookProvider = bookProvider;
        this.eventBus = eventBus;
    }

    public void ingestContent(String bookId) {
        long startTime = System.currentTimeMillis();
        
        String content = bookProvider.getBookText(bookId);
        
        if (content == null) return;

        Document document = new Document(bookId, content);

        datalake.save(document);

        eventBus.publish("document.downloaded", bookId);

        long endTime = System.currentTimeMillis();
        double durationSec = (endTime - startTime) / 1000.0;
        
        double ingestionRate = 1.0 / durationSec; 
        
        System.out.printf("CRAWLER: Document %s processed. Time: %.3f s. Ingestion Rate: %.2f docs/s%n", 
                            bookId, durationSec, ingestionRate);
    }

}
