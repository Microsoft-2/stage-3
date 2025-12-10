package com.microsoft2.bigdata.search.application;

import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;

public class CrawlerService {
    private final DatalakeRepository datalake;
    private final BookProvider bookProvider;

    // Inyectamos el puerto del datalake
    public CrawlerService(DatalakeRepository datalake, BookProvider bookProvider){
        this.datalake = datalake;
        this.bookProvider = bookProvider;
    }

    // Ingestación de contenido
    public String ingestContent(String bookId) {
        // 1. Descargar texto usando el proveedor (Gutendex)
        String content = bookProvider.getBookText(bookId);
        
        if (content == null) return null;

        // 2. Adaptamos a la entidad de dominio
        Document document = new Document(bookId, content);

        // 3. Guardar al datalake
        datalake.save(document);

        System.out.println("Documento " + bookId + " descargado y guardado.");

        return bookId;
    }

}
