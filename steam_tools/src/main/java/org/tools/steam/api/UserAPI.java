package org.tools.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tools.steam.Jogo;

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

            Jogo jogo = null;
            int tentativas = 0;
            int maxTentativas = 3;

            while (jogo == null && tentativas < maxTentativas) {
                jogo = GameAPI.getGame(appid);

                if (jogo == null) {
                    tentativas++;
                    System.out.println("DEU ALGO ERRADO REQUISIÇÃO JOGO: " + appid + " - tentativa " + tentativas + "/" + maxTentativas + ", esperando...");

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return wishList;
                    }
                }
            }

            if (jogo == null) {
                System.out.println("Desisti do jogo " + appid + " após " + maxTentativas + " tentativas.");
                continue;
            }

            jogo.setPrioridade(prioridade);
            wishList.add(jogo);

            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return wishList;
            }
        }

        return wishList;
    }
}
