package com.microsoft2.bigdata;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;

import java.util.Set;

public class MultiMapTest {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Nodo (Simulación) ---");

        // 1. Iniciamos Hazelcast (Infrastructure)
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        // 2. Creamos nuestro Adaptador (Infrastructure -> Domain)
        IndexRepository repo = new HazelcastIndexRepository(hz);

        // 3. Simulamos la acción de un INDEXER (Guardar datos)
        System.out.println("Indexando documentos...");
        repo.save("programacion", "doc_1");
        repo.save("programacion", "doc_2"); // La misma palabra en otro documento
        repo.save("java", "doc_2");
        repo.save("bigdata", "doc_3");

        // 4. Simulamos la acción de un SEARCH ENGINE (Leer datos)
        System.out.println("Buscando 'programacion'...");
        Set<String> resultados = repo.search("programacion");

        // 5. Verificamos
        System.out.println("Resultados encontrados: " + resultados);
        
        if (resultados.contains("doc_1") && resultados.contains("doc_2")) {
            System.out.println("✅ ¡ÉXITO! El MultiMap funciona correctamente.");
        } else {
            System.out.println("❌ ERROR: No se recuperaron los datos esperados.");
        }

        hz.shutdown();
    }
}