package com.microsoft2.bigdata.search.application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    // Updated method to receive JSON content directly (Fixes "File not found" on distributed nodes)
    public void indexDocument(String eventJson) {
        try {
            // 1. Parse the JSON message
            JsonObject json = new Gson().fromJson(eventJson, JsonObject.class);
            String docId = json.get("id").getAsString();
            String content = json.get("content").getAsString();

            // 2. Create the document object directly from the message
            Document doc = new Document(docId, content);

            // 3. Tokenize
            Set<String> tokens = doc.tokenize();

            // 4. Save to Hazelcast
            for (String word : tokens) {
                indexRepository.save(word, doc.getId());
            }

            System.out.println("Indexer: Document " + docId + " indexed remotely (" + tokens.size() + " words).");

        } catch (Exception e) {
            System.err.println("Error indexing document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}