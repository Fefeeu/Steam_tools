package org.tools.steam.api;

import org.json.JSONObject;
import org.tools.steam.Jogo;
import org.tools.steam.api.ApiConnection;

public class GameAPI {

    public static Jogo getGame(int appid) {
        String request = "https://store.steampowered.com/api/appdetails?appids=" + appid;
        JSONObject gamePage = ApiConnection.makeRequestBody(request).getJSONObject(String.valueOf(appid));
        JSONObject gameData = gamePage.getJSONObject("data");

        JSONObject gamePreco = null;
        double preco = -1;
        double promocao = -1;
        boolean jogoLancado = true;
        if (gameData.has("price_overview")) {
            gamePreco = gamePage.getJSONObject("data").getJSONObject("price_overview");
            preco = gamePreco.getDouble("initial") / 100;
            promocao = gamePreco.getDouble("discount_percent") / 100;
        } else {
            jogoLancado = false;
        }

        Jogo jogo = new Jogo(
                gamePage.getJSONObject("data").getString("name"),
                appid,
                preco,
                promocao
        );
        if (jogoLancado) {
            jogo.lancarJogo();
        }

        return jogo;
    }

}
