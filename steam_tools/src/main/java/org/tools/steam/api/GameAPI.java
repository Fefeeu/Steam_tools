package org.tools.steam.api;

import org.json.JSONObject;
import org.tools.steam.Jogo;
import org.tools.steam.api.ApiConnection;

public class GameAPI {

    public static Jogo getGame(String appid) {
        String request = "https://store.steampowered.com/api/appdetails?appids=" + appid;
        JSONObject gamePage = ApiConnection.makeRequestBody(request).getJSONObject(appid);

        Jogo jogo = new Jogo(
                gamePage.getJSONObject("data").getString("name"),
                appid,
                gamePage.getJSONObject("data").getJSONObject("price_overview").getDouble("initial") / 100,
                gamePage.getJSONObject("data").getJSONObject("price_overview").getDouble("discount_percent") / 100
        );
        System.out.println(gamePage.getJSONObject("data").getJSONObject("price_overview").getString("currency"));
        return jogo;
    }

}
