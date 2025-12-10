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
        int maxRedirects = 5;
        
        for (int i = 0; i < maxRedirects; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    // Identificarnos evita errores 403 en algunos servidores
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Java/25") 
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            int status = response.statusCode();

            if (status == 200) {
                return response.body();
            } else if (status >= 300 && status < 400) {
                // Manejar redirección (301, 302, 307)
                String newUrl = response.headers().firstValue("Location").orElse(null);
                
                if (newUrl == null) {
                    throw new RuntimeException("Redirección detectada sin cabecera Location");
                }
                
                // Si la redirección es relativa, resolverla contra la URL original
                if (!newUrl.startsWith("http")) {
                    newUrl = URI.create(url).resolve(newUrl).toString();
                }

                System.out.println("🔄 Redirigiendo (HTTP " + status + ") a: " + newUrl);
                url = newUrl; // Actualizamos la URL y continuamos el bucle
            } else {
                throw new RuntimeException("Error HTTP " + status + " al acceder a " + url);
            }
        }
        
        throw new RuntimeException("Se excedió el límite de redirecciones intentando descargar el libro.");
    }
}