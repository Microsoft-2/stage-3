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
        long startTime = System.currentTimeMillis();

        Document doc = datalake.load(documentId);

        if (doc == null){
            System.err.println("Indexer: Error, document not find: " + documentId);
            return;
        }

        Set<String> tokens = doc.tokenize();
        int tokenCount = tokens.size();

        for (String word : tokens){
            indexRepository.save(word, doc.getId());
        }

        long endTime = System.currentTimeMillis();
        double durationSec = (endTime - startTime) / 1000.0;
        
        double indexingThroughput = tokenCount / durationSec; 
        
        System.out.printf("INDEXER: Document %s indexed. Tokens: %d. Time: %.3f s. Indexing Throughput: %.2f tokens/s%n",
                            documentId, tokenCount, durationSec, indexingThroughput);
    }

}
