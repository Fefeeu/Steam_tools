package org.steam.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.steam.Produto;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ProductAPI {
    public static Produto getJogo(String appid, long diasSemJogar)
    {
        Produto jogo = new Produto(appid, diasSemJogar);
        try {
            String request = "https://store.steampowered.com/api/appdetails?appids=" + appid;
            JSONObject gamePage = ApiConnection.makeRequestBody(request).getJSONObject(appid);


            //pegando nome
            try {
                jogo.setNome(gamePage.getJSONObject("data").getString("name"));
            } catch (JSONException e) {
                System.out.println("Sem nome");
                jogo.setNome(null);
            }


            //pegando dlcs
            try {
                JSONArray DLCs = gamePage.getJSONObject("data").getJSONArray("dlc");
                List<Produto> dlcs = new ArrayList<>();
                for (Object dlc : DLCs) {
                    Produto dlcJson = getJogo(dlc.toString(), 0);
                    compDLC(dlcJson, jogo.getNome());
                    dlcs.add(dlcJson);

                }
                jogo.setDlcs(dlcs);
            } catch (JSONException e) {
                jogo.setDlcs(null);
            }


            //pegando data de lancamento
            try {
                String lancamentoTexto = gamePage.getJSONObject("data").getJSONObject("release_date").getString("date");


                List<DateTimeFormatter> formatadores = Arrays.asList(
                        DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.US),               // "16 Nov, 2009"
                        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),               // "Sep 4, 2013"
                        DateTimeFormatter.ofPattern("d/MMM/yyyy", new Locale("pt", "BR"))    // "13/ago./2013"
                );

                LocalDate lancamento = null;

                for (DateTimeFormatter formatador : formatadores) {
                    try {
                        lancamento = LocalDate.parse(lancamentoTexto, formatador);
                        break;
                    } catch (DateTimeParseException e) {

                    }
                }

                jogo.setDataLancamento(lancamento);

            } catch (JSONException e) {
                System.out.println("Sem Data de lancamento");
                jogo.setDataLancamento(null);
            }


            //pegando preco
            try {
                jogo.setPreco(gamePage.getJSONObject("data").getJSONObject("price_overview").getDouble("final") / 100);
            } catch (JSONException e) {
                jogo.setPreco(00);
            }
            return jogo;
        }catch (JSONException e)
        {
            System.out.println("Limite de requisições alcançada");

        }
        return jogo;
    }

    public static void compDLC(Produto DLC, String NomeJogo)
    {
        try {
            LocalDate hoje = LocalDate.now();
            long lancamento = ChronoUnit.DAYS.between(DLC.getDataLancamento(), hoje);

            System.out.println("O jogo " + NomeJogo + " lançou essa dlc " + DLC.getNome() + " no dia " + DLC.getDataLancamento() + " ela esta custando R$" + DLC.getPreco());

        }catch (NullPointerException e)
        {
            System.out.println("A DLC "+ DLC.getNome() +" não lançou ainda.");
        }
    }


}
