package com.microsoft2.bigdata.search.apps;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.microsoft2.bigdata.search.application.SearchService;
import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastConfigFactory;
import com.microsoft2.bigdata.search.infrastructure.persistence.HazelcastIndexRepository;

import static spark.Spark.get;
import static spark.Spark.port;

public class SearchNode {
    public static void main(String[] args) {
        System.out.println("🔍 INICIANDO SEARCH NODE (API REST)...");

        // Configuración de Puerto (Docker nos pasará el puerto por variable de entorno)
        // Si no, usa el 8080 por defecto
        port(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));

        // 1. Infraestructura
        Config config = HazelcastConfigFactory.createConfig();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);

        // 2. Aplicación
        SearchService searchService = new SearchService(indexRepo);

        // 3. API REST (SparkJava)
        
        // Endpoint: GET /search?q=palabra
        get("/search", (req, res) -> {
            String query = req.queryParams("q");
            if (query == null) {
                res.status(400);
                return "Falta el parámetro 'q'";
            }

            System.out.println("🔍 Búsqueda recibida: " + query);
            return searchService.search(query); // Devuelve JSON automáticamente (Set.toString())
        });

        System.out.println("🔍 Servidor Web listo en puerto " + port());
    }
}