package com.microsoft2.bigdata.search.infrastructure.datalake;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;

public class HazelcastDatalake implements DatalakeRepository {
    private final IMap<String, String> distributedStore;

    public HazelcastDatalake(HazelcastInstance hz) {
        // This map is synchronized across ALL computers in the cluster
        this.distributedStore = hz.getMap("datalake-storage");
    }

    @Override
    public void save(Document document) {
        // PC1 saves content here, and it is automatically accessible to PC2
        distributedStore.put(document.getId(), document.getContent());
    }

    @Override
    public Document load(String documentId) {
        // PC2 reads content from here (fetching it from PC1 if needed)
        String content = distributedStore.get(documentId);
        if (content == null) return null;
        return new Document(documentId, content);
    }
}
