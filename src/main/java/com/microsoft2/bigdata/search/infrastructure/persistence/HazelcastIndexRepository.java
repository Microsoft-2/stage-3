package com.microsoft2.bigdata.search.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.multimap.MultiMap;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;

import java.util.HashSet;
import java.util.Set;

public class HazelcastIndexRepository implements IndexRepository{
    private final HazelcastInstance hazelcastInstance;

    public HazelcastIndexRepository(HazelcastInstance hazelcastInstance){
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void save(String word, String documentId) {
        MultiMap<String, String> index = hazelcastInstance.getMultiMap("inverted-index");

        index.put(word, documentId);
    }

    @Override
    public Set<String> search(String word){
        MultiMap<String, String> index = hazelcastInstance.getMultiMap("inverted-index");

        return new HashSet<>(index.get(word));
    }
}
