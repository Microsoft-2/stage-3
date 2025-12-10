package com.microsoft2.bigdata;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.*;
import com.microsoft2.bigdata.search.domain.ports.*;
import com.microsoft2.bigdata.search.infrastructure.api.GutendexAdapter;
import com.microsoft2.bigdata.search.infrastructure.persistence.*;

import java.util.Set;

public class GutendexTest {
    public static void main(String[] args) {
        System.out.println("--- INICIANDO BUSCADOR CON GUTENDEX ---");

        // 1. INFRAESTRUCTURA
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);
        DatalakeRepository datalakeRepo = new FileSystemDatalake("datalake_store");
        BookProvider gutendex = new GutendexAdapter(); // <--- Nuevo adaptador

        // 2. APLICACIÓN
        // Le pasamos el adaptador de Gutendex al Crawler
        CrawlerService crawler = new CrawlerService(datalakeRepo, gutendex);
        IndexerService indexer = new IndexerService(datalakeRepo, indexRepo);
        SearchService searchEngine = new SearchService(indexRepo);

        // 3. EJECUCIÓN DEL FLUJO

        // A) CRAWLER: Descarga "Las Aventuras de Sherlock Holmes" (ID: 1661)
        // Nota: Esto tardará unos segundos porque hace peticiones a internet real
        String bookId = "1661"; 
        crawler.ingestContent(bookId);

        // B) INDEXER: Procesa el libro (Son más de 100.000 palabras)
        System.out.println("Indexando... (esto puede tardar un poco)");
        indexer.indexDocument(bookId);

        // C) SEARCH: Buscamos algo que sabemos que está en Sherlock Holmes
        System.out.println("--- BUSCANDO ---");
        Set<String> result1 = searchEngine.search("elementary");
        Set<String> result2 = searchEngine.search("watson");
        
        System.out.println("Documentos con 'elementary': " + result1);
        System.out.println("Documentos con 'watson': " + result2);

        // Verificación
        if (result1.contains(bookId) && result2.contains(bookId)) {
            System.out.println("✅ PRUEBA SUPERADA: Sherlock Holmes ha sido indexado correctamente.");
        } else {
            System.out.println("❌ ALGO FALLÓ: No se encontró el libro.");
        }

        hz.shutdown();
    }
}