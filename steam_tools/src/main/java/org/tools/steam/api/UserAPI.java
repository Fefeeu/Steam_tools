package org.tools.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tools.steam.Jogo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class UserAPI {

    private static JSONArray getWishListItems(String userId){

        JSONObject respostaJson = ApiConnection.makeRequestBody(
                "https://api.steampowered.com/IWishlistService/GetWishlist/v1?steamid=" + userId
        );

        JSONArray jogos = respostaJson.getJSONObject("response").getJSONArray("items");

        return jogos;
    }

    public static List<Jogo> getWishlist(String userId){

        List<Jogo> wishList = new ArrayList<>();

        JSONArray jogosJson = getWishListItems(userId);

        int tamanhoWishList = jogosJson.length();

        for (int i = 0; i < tamanhoWishList; i++){
            JSONObject jogoAtual = jogosJson.getJSONObject(i);

            int appid = jogoAtual.getInt("appid");
            int prioridade = jogoAtual.getInt("priority");

            Jogo jogo = GameAPI.getGame(appid);
            jogo.setPrioridade(prioridade);

            wishList.add(jogo);
        }

        return wishList;
    }
}
