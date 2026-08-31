package org.tools.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tools.ConfigLoader;

import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryAPI {
    private static final String API_KEY = ConfigLoader.get("steam.api.key");

    public static Map<String, String> getOwnedGames (String steamId) {
        String request = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/"
                + "?key=" + API_KEY
                + "&steamid=" + steamId
                + "&include_appinfo=true"
                + "&format=json";
        JSONObject resposta = ApiConnection.makeRequestBody(request);
        Map<String, String> jogos = new LinkedHashMap<>();

        if (!resposta.has("response") || !resposta.getJSONObject("response").has("games")){
            return jogos;
        }
        JSONArray games =  resposta.getJSONObject("response").getJSONArray("games");
        for (int i = 0; i < games.length(); i++) {
            JSONObject jogo =  games.getJSONObject(i);
            jogos.put(String.valueOf(jogo.getInt("appid")), jogo.getString("name"));
        }

        return jogos;
    }
}
