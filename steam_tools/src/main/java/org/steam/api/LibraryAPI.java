package org.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ConfigLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryAPI {
    private static final String API_KEY = ConfigLoader.get("steam.api.key");
    private static final boolean USE_MOCK = true; // troque pra false pra usar a API real

    public static Map<String, String> getOwnedGames(String steamId) {
        if (USE_MOCK) {
            return getMockOwnedGames(steamId);
        }

        String request = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/"
                + "?key=" + API_KEY
                + "&steamid=" + steamId
                + "&include_appinfo=true"
                + "&format=json";
        JSONObject resposta = ApiConnection.makeRequestBody(request);
        return parseJogos(resposta);
    }

    private static Map<String, String> getMockOwnedGames(String steamId) {
        String caminho = "mock/" + steamId + ".json";

        try (InputStream input = LibraryAPI.class.getClassLoader().getResourceAsStream(caminho)) {
            if (input == null) {
                System.out.println("Aviso: mock não encontrado para " + steamId + " (" + caminho + ")");
                return new LinkedHashMap<>();
            }
            String conteudo = new String(input.readAllBytes());
            JSONObject resposta = new JSONObject(conteudo);
            return parseJogos(resposta);
        } catch (IOException e) {
            System.out.println("Erro ao ler mock de " + steamId + ": " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private static Map<String, String> parseJogos(JSONObject resposta) {
        Map<String, String> jogos = new LinkedHashMap<>();

        if (!resposta.has("response") || !resposta.getJSONObject("response").has("games")) {
            return jogos;
        }
        JSONArray games = resposta.getJSONObject("response").getJSONArray("games");
        for (int i = 0; i < games.length(); i++) {
            JSONObject jogo = games.getJSONObject(i);
            jogos.put(String.valueOf(jogo.getInt("appid")), jogo.getString("name"));
        }
        return jogos;
    }
}