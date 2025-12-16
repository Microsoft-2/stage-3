// Contenido actualizado para IndexerService.java

package com.microsoft2.bigdata.search.application;

import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;

import java.util.Set;

public class IndexerService {
    private final DatalakeRepository datalake;
    private final IndexRepository indexRepository;

    public IndexerService(DatalakeRepository datalake, IndexRepository indexRepository){
        this.datalake = datalake;
        this.indexRepository = indexRepository;
    }

    public void indexDocument(String documentId){
        long startTime = System.currentTimeMillis(); // <-- INICIO DE MEDICIÓN

        // 1. Recuperar el documento completo del Datalake
        Document doc = datalake.load(documentId);

        if (doc == null){
            System.err.println("Indexer: Error, document not find: " + documentId);
            return;
        }

        // 2. Tokenizar (usando la lógica de la entidad Document)
        Set<String> tokens = doc.tokenize();
        int tokenCount = tokens.size(); // Contar tokens para la métrica

        // 3. Guardar cada palabra en el índice invertido
        for (String word : tokens){
            indexRepository.save(word, doc.getId());
        }

        long endTime = System.currentTimeMillis(); // <-- FIN DE MEDICIÓN
        double durationSec = (endTime - startTime) / 1000.0;
        
        // CÁLCULO Y REPORTE
        // tokens/s = Total de tokens / tiempo_en_segundos
        double indexingThroughput = tokenCount / durationSec; 
        
        // Logueamos los resultados en la terminal del contenedor
        System.out.printf("INDEXER: Document %s indexed. Tokens: %d. Time: %.3f s. Indexing Throughput: %.2f tokens/s%n",
                            documentId, tokenCount, durationSec, indexingThroughput);
    }
}