package org.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.steam.Jogo;
import org.steam.Produto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class UserAPI {

    private static JSONArray getListaJogos(String userId){

        JSONObject respostaJson = ApiConnection.makeRequestBody(
                        "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=194FB210F5B1AFFEF7AADDB3C6FF84F9&steamid=" + userId
        );

        JSONArray jogos = respostaJson.getJSONObject("response").getJSONArray("games");

        return jogos;
    }

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

    public static Produto[] getJogos(String userId){

        JSONArray jogosJson = getListaJogos(userId);

        int tamanholista = jogosJson.length();
        Produto[] listaJogos = new Produto[tamanholista];

        for (int i = 0; i < tamanholista; i++){
            JSONObject jogoAtual = jogosJson.getJSONObject(i);

            String appid = String.valueOf(jogoAtual.getInt("appid"));


            LocalDate dataUltimoJogo = Instant.ofEpochSecond(jogoAtual.getInt("rtime_last_played"))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate hoje = LocalDate.now();

            long diasSemJogar = ChronoUnit.DAYS.between(dataUltimoJogo, hoje);


            Produto jogo = new Produto(appid, diasSemJogar);
            listaJogos[i] = jogo;

        }

        return listaJogos;
    }
}
