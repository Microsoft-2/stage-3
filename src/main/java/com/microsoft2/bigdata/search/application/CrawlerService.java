// Contenido actualizado para CrawlerService.java

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

    // Ingestación de contenido con medición de tiempo
    public void ingestContent(String bookId) {
        long startTime = System.currentTimeMillis(); // <-- INICIO DE MEDICIÓN
        
        // 1. Descargar texto usando el proveedor (Gutendex)
        String content = bookProvider.getBookText(bookId);
        
        if (content == null) return;

        // 2. Adaptamos a la entidad de dominio
        Document document = new Document(bookId, content);

        // 3. Guardar al datalake
        datalake.save(document);

        // Nombre del topic: document.downloaded
        eventBus.publish("document.downloaded", bookId);

        long endTime = System.currentTimeMillis(); // <-- FIN DE MEDICIÓN
        double durationSec = (endTime - startTime) / 1000.0;
        
        // CÁLCULO Y REPORTE
        // docs/s = 1 documento / tiempo_en_segundos
        double ingestionRate = 1.0 / durationSec; 
        
        // Logueamos los resultados en la terminal del contenedor
        System.out.printf("CRAWLER: Document %s processed. Time: %.3f s. Ingestion Rate: %.2f docs/s%n", 
                            bookId, durationSec, ingestionRate);
    }
}