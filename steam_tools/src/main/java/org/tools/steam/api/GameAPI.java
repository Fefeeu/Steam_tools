package org.tools.steam.api;

import org.json.JSONObject;
import org.tools.steam.Jogo;
import org.tools.steam.api.ApiConnection;

public class GameAPI {

    public static Jogo getGame(int appid) {
        String request = "https://store.steampowered.com/api/appdetails?appids=" + appid;
        JSONObject gamePage = ApiConnection.makeRequestBody(request).getJSONObject(String.valueOf(appid));
        System.out.println(gamePage.getJSONObject("data").getString("name"));
        JSONObject gamePreco = gamePage.getJSONObject("data").getJSONObject("price_overview");

        Jogo jogo = new Jogo(
                gamePage.getJSONObject("data").getString("name"),
                appid,
                gamePreco.getDouble("initial") / 100,
                gamePreco.getDouble("discount_percent") / 100
        );
        return jogo;
    }

}
