package com.microsoft2.bigdata.search.domain.ports;

import java.util.Set;

// Puerto de Salida: Define que necesitamos del sistema de indexación
public interface IndexRepository {
    // Guardar una palabra asociada a un documento
    void save(String word, String documentId);

    // Buscar documentos que contengan otra palabra
    Set<String> search(String word);
}
