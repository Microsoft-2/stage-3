package com.microsoft2.bigdata.search.domain.ports;

import com.microsoft2.bigdata.search.domain.model.Document;

// El Crawler usará esto para guardar, el Indexer para leer.
public interface DatalakeRepository {
    void save(Document document);
    Document load(String documentId);
}