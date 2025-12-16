package com.microsoft2.bigdata.search.application;

import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;
import com.microsoft2.bigdata.search.domain.ports.EventBus;

public class CrawlerService {
    private final DatalakeRepository datalake;
    private final BookProvider bookProvider;
    private final EventBus eventBus;

    // Inyectamos el puerto del datalake
    public CrawlerService(DatalakeRepository datalake, BookProvider bookProvider, EventBus eventBus){
        this.datalake = datalake;
        this.bookProvider = bookProvider;
        this.eventBus = eventBus;
    }

    // Ingestación de contenido
    public void ingestContent(String bookId) {
        // 1. Descargar texto usando el proveedor (Gutendex)
        String content = bookProvider.getBookText(bookId);
        
        if (content == null) return;

        // 2. Adaptamos a la entidad de dominio
        Document document = new Document(bookId, content);

        // 3. Guardar al datalake
        datalake.save(document);

        System.out.println("Document " + bookId + " downloaded and saved.");

        // Nombre del topic: document.downloaded
        eventBus.publish("document.downloaded", bookId);
    }

}
