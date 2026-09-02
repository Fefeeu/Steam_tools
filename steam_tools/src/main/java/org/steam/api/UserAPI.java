package org.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.steam.Produto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class UserAPI {

    private static JSONArray getListaJogos(String userId){

        JSONObject respostaJson = ApiConnection.makeRequestBody(
                        "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=194FB210F5B1AFFEF7AADDB3C6FF84F9&steamid=" + userId
        );

        JSONArray jogos = respostaJson.getJSONObject("response").getJSONArray("games");

        return jogos;
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
