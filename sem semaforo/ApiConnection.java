package org.steam.api;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * VERSÃO SEM SEMÁFORO: toda thread faz a requisição na hora, sem controle de ritmo.
 */
public class ApiConnection {

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static JSONObject makeRequestBody(String endPoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endPoint))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENTE.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                System.out.println("[ApiConnection] 429 (limite de requisições da Steam) em: " + endPoint);
            } else if (response.statusCode() != 200) {
                System.out.println("[ApiConnection] HTTP " + response.statusCode() + " em: " + endPoint);
            }

            return new JSONObject(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println(e);
        }
        return new JSONObject();
    }
}
