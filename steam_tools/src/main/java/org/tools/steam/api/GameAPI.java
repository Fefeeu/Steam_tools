package org.tools.steam.api;

import org.json.JSONObject;
import org.json.JSONException;
import org.tools.steam.Jogo;
import org.tools.steam.api.ApiConnection;

public class GameAPI {

    public static Jogo getGame(int appid) {
        try {
            String request = "https://store.steampowered.com/api/appdetails?appids=" + appid;
            JSONObject resposta = ApiConnection.makeRequestBody(request);

            if (!resposta.has(String.valueOf(appid))) {
                System.out.println("[GameAPI] appid " + appid + " não veio na resposta (possível rate limit).");
                return null;
            }

            JSONObject gamePage = resposta.getJSONObject(String.valueOf(appid));

            if (!gamePage.optBoolean("success", false) || !gamePage.has("data")) {
                System.out.println("[GameAPI] appid " + appid + " respondeu success=false ou sem 'data'.");
                return null;
            }

            JSONObject gameData = gamePage.getJSONObject("data");

            double preco = -1;
            double promocao = -1;
            boolean jogoLancado = true;

            if (gameData.has("price_overview")) {
                JSONObject gamePreco = gameData.getJSONObject("price_overview");
                preco = gamePreco.getDouble("initial") / 100;
                promocao = gamePreco.getDouble("discount_percent") / 100;
            } else {
                jogoLancado = false;
            }

            Jogo jogo = new Jogo(
                    gameData.getString("name"),
                    appid,
                    preco,
                    promocao
            );
            if (jogoLancado) {
                jogo.lancarJogo();
            }

            return jogo;

        } catch (JSONException e) {
            System.out.println("[GameAPI] appid " + appid + " deu JSONException: " + e.getMessage());
            return null;
        }
    }

}