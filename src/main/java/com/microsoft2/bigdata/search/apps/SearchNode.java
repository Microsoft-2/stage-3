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
        System.out.println("STARTING SEARCH NODE (API REST)...");

        port(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));

        Config config = HazelcastConfigFactory.createConfig();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IndexRepository indexRepo = new HazelcastIndexRepository(hz);

        SearchService searchService = new SearchService(indexRepo);

        get("/search", (req, res) -> {
            String query = req.queryParams("q");
            if (query == null) {
                res.status(400);
                return "Parameter 'q' missing";
            }

            System.out.println("Search query received: " + query);
            return searchService.search(query);
        });

        System.out.println("Server Web ready on port " + port());
    }
}