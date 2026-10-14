package org.steam.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.steam.Jogo;
import org.steam.Produto;
import org.steam.util.JsonExporter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class UserAPI {

    // A chave NÃO fica mais no código. Defina a variável de ambiente STEAM_API_KEY
    // (ou troque por uma leitura do seu ConfigLoader).
    private static String chave() {
        String k = System.getenv("STEAM_API_KEY");
        if (k == null || k.isEmpty()) {
            throw new IllegalStateException("Defina a variável de ambiente STEAM_API_KEY com a sua chave da Steam.");
        }
        return k;
    }

    private static JSONArray getListaJogos(String userId) {

        JSONObject respostaJson = ApiConnection.makeRequestBody(
                "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + chave() + "&steamid=" + userId
        );

        if (respostaJson == null) {
            System.out.println("[Biblioteca] Sem resposta da Steam para o usuário " + userId);
            return new JSONArray();
        }

        JSONObject response = respostaJson.optJSONObject("response");
        if (response == null || !response.has("games")) {
            System.out.println("[Biblioteca] Sem 'games' (perfil privado?) para o usuário " + userId);
            return new JSONArray();
        }

        return response.getJSONArray("games");
    }

    // Uma requisição: devolve os itens (appid + priority) da lista de desejos.
    public static JSONArray getWishListItems(String userId) {

        JSONObject respostaJson = ApiConnection.makeRequestBody(
                "https://api.steampowered.com/IWishlistService/GetWishlist/v1?steamid=" + userId
        );

        if (respostaJson == null) {
            return new JSONArray();
        }

        JSONObject response = respostaJson.optJSONObject("response");
        if (response == null || !response.has("items")) {
            System.out.println("[Wishlist] Resposta sem 'items' para o usuário " + userId);
            return new JSONArray();
        }

        return response.getJSONArray("items");
    }

    // Uma requisição: busca os dados de um jogo da lista de desejos. Retorna null se falhar.
    public static Jogo buscarJogoDaWishlist(int appid, int prioridade) {
        Jogo jogo = GameAPI.getGame(appid);
        if (jogo != null) {
            jogo.setPrioridade(prioridade);
        }
        return jogo;
    }

    public static List<Jogo> getWishlist(String userId) {

        List<Jogo> wishList = new ArrayList<>();

        JSONArray jogosJson = getWishListItems(userId);

        for (int i = 0; i < jogosJson.length(); i++) {
            JSONObject jogoAtual = jogosJson.getJSONObject(i);

            int appid = jogoAtual.getInt("appid");
            int prioridade = jogoAtual.getInt("priority");

            Jogo jogo = null;
            int tentativas = 0;
            int maxTentativas = 3;

            while (jogo == null && tentativas < maxTentativas) {
                jogo = buscarJogoDaWishlist(appid, prioridade);

                if (jogo == null) {
                    tentativas++;
                    System.out.println("DEU ALGO ERRADO REQUISIÇÃO JOGO: " + appid
                            + " - tentativa " + tentativas + "/" + maxTentativas + ", esperando...");

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

            wishList.add(jogo);
            // Sem Thread.sleep aqui: o ritmo agora é controlado pelo semáforo no ApiConnection.
        }

        return wishList;
    }

    public static Produto[] getJogos(String userId) {

        JSONArray jogosJson = getListaJogos(userId);

        int tamanholista = jogosJson.length();
        Produto[] listaJogos = new Produto[tamanholista];

        LocalDate hoje = LocalDate.now();

        for (int i = 0; i < tamanholista; i++) {
            JSONObject jogoAtual = jogosJson.getJSONObject(i);

            String appid = String.valueOf(jogoAtual.getInt("appid"));

            LocalDate dataUltimoJogo = Instant.ofEpochSecond(jogoAtual.optLong("rtime_last_played", 0))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            long diasSemJogar = ChronoUnit.DAYS.between(dataUltimoJogo, hoje);

            listaJogos[i] = new Produto(appid, diasSemJogar);
        }

        return listaJogos;
    }

    // Lê o JSON local. Não é mais usado pelo controller.
    public static Produto[] getJogosDoJsonLocal() {
        return JsonExporter.lerListaProdutosDoJson();
    }
}