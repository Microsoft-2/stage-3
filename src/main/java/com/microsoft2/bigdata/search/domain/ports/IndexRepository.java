package com.microsoft2.bigdata.search.domain.ports;

import java.util.Set;

public interface IndexRepository {
    void save(String word, String documentId);

    Set<String> search(String word);
}
