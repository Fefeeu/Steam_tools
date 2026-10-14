package org.steam.api;

import org.json.JSONObject;
import org.steam.util.LimitadorApi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiConnection {

    // Um único cliente compartilhado (o HttpClient é thread-safe)
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Ponto único de saída para a Steam. Toda requisição de qualquer thread
     * passa pelo semáforo correspondente ao endpoint.
     * Continua devolvendo um JSONObject vazio em caso de falha (nunca null).
     */
    public static JSONObject makeRequestBody(String endPoint) {
        LimitadorApi limitador = endPoint.contains("store.steampowered.com")
                ? LimitadorApi.STORE
                : LimitadorApi.STEAM_API;

        JSONObject resultado = limitador.executar(() -> makeRequestBodySemLimite(endPoint));

        // executar() devolve null se a thread foi interrompida enquanto esperava a permissão
        return resultado != null ? resultado : new JSONObject();
    }

    private static JSONObject makeRequestBodySemLimite(String endPoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endPoint))
                    .timeout(Duration.ofSeconds(20))   // não segura a permissão para sempre
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