package com.microsoft2.bigdata.search.infrastructure.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft2.bigdata.search.domain.ports.BookProvider;

public class GutendexAdapter implements BookProvider {
    private static final String API_URL = "https://gutendex.com/books/?ids=";
    private final HttpClient client;

    public GutendexAdapter() {
        // Usamos una configuración básica, la redirección la manejaremos manualmente para más control
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getBookText(String bookId) {
        try {
            System.out.println("Consultando metadatos para libro ID: " + bookId);
            String jsonResponse = fetch(API_URL + bookId);
            
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            if (!json.has("count") || json.get("count").getAsInt() == 0) {
                System.err.println("❌ Libro no encontrado en Gutendex API: " + bookId);
                return null;
            }

            JsonObject book = json.getAsJsonArray("results").get(0).getAsJsonObject();
            JsonObject formats = book.getAsJsonObject("formats");
            
            String textUrl = null;
            for (String key : formats.keySet()) {
                if (key.startsWith("text/plain")) {
                    textUrl = formats.get(key).getAsString();
                    System.out.println("✅ Formato encontrado (" + key + "): " + textUrl);
                    break;
                }
            }

            if (textUrl == null) {
                System.err.println("❌ El libro existe pero no tiene formato text/plain disponible.");
                return null;
            }

            System.out.println("⬇️ Descargando contenido del libro...");
            return fetch(textUrl);

        } catch (Exception e) {
            System.err.println("❌ Error en GutendexAdapter: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String fetch(String url) throws Exception {
        int maxRetries = 5;
        int waitTime = 10000; // Empezamos esperando 10 segundos si falla

        for (int i = 0; i < maxRetries; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "Mozilla/5.0 (Education Project; ULPGC)") // Sé amable identificándote
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 200) {
                return response.body();
            } 
            else if (status == 429) {
                // ERROR 429: BLOQUEO TEMPORAL POR EXCESO DE PETICIONES
                System.err.println("⛔ Rate Limit (429) detectado. Esperando " + (waitTime/1000) + "s antes de reintentar...");
                Thread.sleep(waitTime);
                waitTime *= 2; // Exponential Backoff: Esperamos el doble la próxima vez (10s, 20s, 40s...)
                continue; // Volvemos a intentar la misma petición
            }
            else if (status >= 300 && status < 400) {
                // Manejo de Redirecciones (Como ya tenías)
                String newUrl = response.headers().firstValue("Location").orElse(null);
                if (newUrl == null) throw new RuntimeException("Redirección sin cabecera Location");
                if (!newUrl.startsWith("http")) newUrl = URI.create(url).resolve(newUrl).toString();
                
                System.out.println("🔄 Redirigiendo a: " + newUrl);
                url = newUrl; 
            } 
            else {
                throw new RuntimeException("Error HTTP " + status + " al acceder a " + url);
            }
        }
        throw new RuntimeException("Se excedió el límite de reintentos para: " + url);
    }
}