package org.tools.steam.api;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiConnection {
    public static JSONObject makeRequestBody(String endPoint){
        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endPoint))
                    .GET()
                    .build();

            HttpResponse<String> response = cliente.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject respostaJson = new JSONObject(response.body());
            return respostaJson;
        } catch (Exception e){
            System.out.println(e);
        }
        return new JSONObject();
    }
}
