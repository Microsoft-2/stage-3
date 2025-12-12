package com.microsoft2.bigdata.search.infrastructure.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GutendexAdapter implements BookProvider {
    private static final String API_URL = "https://gutendex.com/books/?ids=";
    private final HttpClient client;

    public GutendexAdapter() {
        // MEJORA: Timeout de 60 segundos para evitar errores con internet lento
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public String getBookText(String bookId) {
        try {
            System.out.println("Consultando API Gutendex para libro ID: " + bookId);
            String jsonResponse = fetch(API_URL + bookId);
            
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            if (!json.has("count") || json.get("count").getAsInt() == 0) {
                System.err.println("Libro no encontrado en Gutendex API: " + bookId);
                return null;
            }

            JsonObject book = json.getAsJsonArray("results").get(0).getAsJsonObject();
            JsonObject formats = book.getAsJsonObject("formats");
            
            String textUrl = null;
            for (String key : formats.keySet()) {
                if (key.startsWith("text/plain")) {
                    textUrl = formats.get(key).getAsString();
                    break;
                }
            }

            if (textUrl == null) {
                System.err.println("El libro " + bookId + " existe pero no tiene texto plano.");
                return null;
            }

            System.out.println("Descargando texto real...");
            return fetch(textUrl);

        } catch (Exception e) {
            System.err.println("Error descargando libro " + bookId + ": " + e.getMessage());
            // No lanzamos excepción para que el Crawler siga con el siguiente libro
            return null;
        }
    }

    private String fetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", "Mozilla/5.0 (Student Project; ULPGC)") 
                .timeout(Duration.ofSeconds(60)) // Timeout también en la request
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("Error HTTP " + response.statusCode());
        }
    }
}
