package com.microsoft2.bigdata.search.domain.ports;

import com.microsoft2.bigdata.search.domain.model.Document;

public interface DatalakeRepository {
    void save(Document document);
    Document load(String documentId);
}
