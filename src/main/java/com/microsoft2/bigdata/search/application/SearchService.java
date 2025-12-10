package com.microsoft2.bigdata.search.application;

import com.microsoft2.bigdata.search.domain.ports.IndexRepository;
import java.util.Set;

public class SearchService {
    private final IndexRepository indexRepository;

    public SearchService(IndexRepository indexRepository){
        this.indexRepository = indexRepository;
    }

    public Set<String> search(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        if(normalizedQuery.isEmpty()){
            return Set.of();
        }

        return indexRepository.search(normalizedQuery);
    }
}
