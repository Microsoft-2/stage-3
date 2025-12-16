package com.microsoft2.bigdata.search.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.multimap.MultiMap;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;

import java.util.HashSet;
import java.util.Set;

public class HazelcastIndexRepository implements IndexRepository{
    // Referencia a la instancia real de Hazelcast
    private final HazelcastInstance hazelcastInstance;

    public HazelcastIndexRepository(HazelcastInstance hazelcastInstance){
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void save(String word, String documentId) {
        // Obtenemos el Multi-Map inverted index
        MultiMap<String, String> index = hazelcastInstance.getMultiMap("inverted-index");

        // Guardamos la relación. Si ya existe, añade el nuevo docId a la lista.
        // Esto es Thread-Safe y distribuido automáticamente.
        index.put(word, documentId);
    }

    @Override
    public Set<String> search(String word){
        MultiMap<String, String> index = hazelcastInstance.getMultiMap("inverted-index");

        // Obtenemos la colección de documentos para esa palabra
        // Convertimos a Set para cumplir con nuestro contrato de Dominio limpio
        return new HashSet<>(index.get(word));
    }
}
