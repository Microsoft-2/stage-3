package com.microsoft2.bigdata.search.infrastructure.datalake;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;

public class HazelcastDatalake implements DatalakeRepository {
    private final IMap<String, String> distributedStore;

    public HazelcastDatalake(HazelcastInstance hz) {
        this.distributedStore = hz.getMap("datalake-storage");
    }

    @Override
    public void save(Document document) {
        distributedStore.put(document.getId(), document.getContent());
    }

    @Override
    public Document load(String documentId) {
        String content = distributedStore.get(documentId);
        if (content == null) return null;
        return new Document(documentId, content);
    }
}
