package com.microsoft2.bigdata.search.application;

import java.util.UUID;

import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;

public class CrawlerService {
    private final DatalakeRepository datalake;

    // Inyectamos el puerto del datalake
    public CrawlerService(DatalakeRepository datalake){
        this.datalake = datalake;
    }

    // Ingestación de contenido
    public String ingestContent(String content, String sourceUrl) {
        // 1. Generar un ID único para el documento
        String id = UUID.randomUUID().toString();

        // 2. Adaptamos a la entidad de dominio
        Document document = new Document(id, content);

        // 3. Guardar al datalake
        datalake.save(document);

        System.out.println("Documento " + id + " descargado y guardado.");

        return id;
    }

}
