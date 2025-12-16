package com.microsoft2.bigdata;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;

import java.util.Set;

public class MultiMapTest {
    public static void main(String[] args) {
        System.out.println("--- Starting Node ---");

        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        IndexRepository repo = new HazelcastIndexRepository(hz);

        System.out.println("Indexingd documents");
        repo.save("programming", "doc_1");
        repo.save("programming", "doc_2");
        repo.save("java", "doc_2");
        repo.save("bigdata", "doc_3");

        System.out.println("Searching 'programming'");
        Set<String> resultados = repo.search("programming");

        System.out.println("Results found: " + results);
        
        if (results.contains("doc_1") && results.contains("doc_2")) {
            System.out.println("MultiMap is working.");
        } else {
            System.out.println("ERROR: Counld not find the data.");
        }

        hz.shutdown();
    }
}
