package com.microsoft2.bigdata;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.CrawlerService;
import com.microsoft2.bigdata.search.application.IndexerService;
import com.microsoft2.bigdata.search.application.SearchService;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.persistence.FileSystemDatalake;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;

import java.util.Set;

public class DatalakeTest {
    public static void main(String[] args) {
        // 1. INFRAESTRUCTURA (El "Hardware" del software)
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);
        DatalakeRepository datalakeRepo = new FileSystemDatalake("datalake_store"); // Guardará en carpeta 'datalake_store'

        // 2. APLICACIÓN (La Lógica / Servicios)
        CrawlerService crawler = new CrawlerService(datalakeRepo);
        IndexerService indexer = new IndexerService(datalakeRepo, indexRepo);
        SearchService searchEngine = new SearchService(indexRepo);

        System.out.println("--- 1. CRAWLER INGESTANDO ---");
        // Simulamos que el Crawler descarga un libro de Cervantes
        String content = "En un lugar de la Mancha de cuyo nombre no quiero acordarme";
        String docId = crawler.ingestContent(content, "cervantes.txt");

        System.out.println("--- 2. INDEXER PROCESANDO ---");
        // Manualmente le decimos al indexer que procese ese ID (luego esto será automático con ActiveMQ)
        indexer.indexDocument(docId);

        System.out.println("--- 3. SEARCH ENGINE BUSCANDO ---");
        Set<String> result = searchEngine.search("mancha");
        
        System.out.println("Documentos que contienen 'mancha': " + result);

        // Limpieza
        hz.shutdown();
    }
}