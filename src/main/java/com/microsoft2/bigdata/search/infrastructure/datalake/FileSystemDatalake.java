package com.microsoft2.bigdata.search.infrastructure.datalake;

import com.microsoft2.bigdata.search.domain.model.Document;
import com.microsoft2.bigdata.search.domain.ports.DatalakeRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemDatalake implements DatalakeRepository {
    private final String storagePath;

    public FileSystemDatalake(String storagePath) {
        this.storagePath = storagePath;
        new File(storagePath).mkdirs(); // Crear carpeta si no existe
    }

    @Override
    public void save(Document document) {
        try {
            Path path = Paths.get(storagePath, document.getId() + ".txt");
            Files.writeString(path, document.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Document load(String documentId) {
        try {
            Path path = Paths.get(storagePath, documentId + ".txt");
            if (!Files.exists(path)) return null;
            String content = Files.readString(path);
            return new Document(documentId, content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}