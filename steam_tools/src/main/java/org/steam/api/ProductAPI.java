package org.steam.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.steam.Produto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProductAPI {

    private static final String NOME_PADRAO = "Sem nome";

    private static final List<DateTimeFormatter> FORMATADORES = Arrays.asList(
            DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("d/MMM/yyyy", new Locale("pt", "BR")),
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"))
    );

    /**
     * Busca um jogo direto na Steam.
     * O controle de ritmo (semáforo) está no ApiConnection.makeRequestBody,
     * por isso aqui não há nenhum acquire/release.
     */
    public static Produto getJogo(String appid, long diasSemJogar) {
        Produto jogo = new Produto(appid, diasSemJogar);
        try {
            String request = "https://store.steampowered.com/api/appdetails?appids=" + appid;
            JSONObject response = ApiConnection.makeRequestBody(request);

            if (response == null || !response.has(appid)) {
                System.out.println("Resposta da API vazia ou inválida para o appid: " + appid);
                return jogo;
            }

            JSONObject gamePage = response.getJSONObject(appid);

            if (!gamePage.optBoolean("success", false)) {
                System.out.println("Steam reportou falha ao buscar o appid: " + appid);
                return jogo;
            }

            JSONObject data = gamePage.optJSONObject("data");
            if (data == null) return jogo;

            // nome
            String nome = data.optString("name", "");
            if (!nome.isEmpty()) {
                jogo.setNome(nome);
            }

            // data de lançamento
            JSONObject releaseDate = data.optJSONObject("release_date");
            if (releaseDate != null) {
                jogo.setDataLancamento(converterData(releaseDate.optString("date", "")));
            }

            // preço
            JSONObject priceOverview = data.optJSONObject("price_overview");
            jogo.setPreco(priceOverview != null ? priceOverview.optDouble("final", 0.0) / 100.0 : 0.0);

            // DLCs (cada uma é uma requisição extra, e passa pelo semáforo)
            JSONArray dlcsArray = data.optJSONArray("dlc");
            if (dlcsArray != null) {
                List<Produto> dlcs = new ArrayList<>();
                for (int i = 0; i < dlcsArray.length(); i++) {
                    String dlcId = dlcsArray.get(i).toString();
                    Produto dlc = getJogo(dlcId, 0);

                    if (nomeValido(dlc)) {
                        compDLC(dlc, jogo.getNome());
                        dlcs.add(dlc);
                    } else {
                        System.out.println("Falha ao buscar DLC " + dlcId + " do jogo " + appid);
                    }
                }
                jogo.setDlcs(dlcs);
            } else {
                jogo.setDlcs(Collections.emptyList());
            }

            return jogo;

        } catch (JSONException e) {
            System.err.println("Erro ao processar JSON (limite de requisições ou formato inválido): " + e.getMessage());
        }

        return jogo;
    }

    public static boolean nomeValido(Produto p) {
        return p != null
                && p.getNome() != null
                && !p.getNome().isEmpty()
                && !NOME_PADRAO.equals(p.getNome());
    }

    private static LocalDate converterData(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatador : FORMATADORES) {
            try {
                return LocalDate.parse(texto, formatador);
            } catch (DateTimeParseException ignored) {
                // tenta o próximo formatador
            }
        }
        System.out.println("Não foi possível converter a data: " + texto);
        return null;
    }

    public static void compDLC(Produto dlc, String nomeJogo) {
        if (dlc.getDataLancamento() == null) {
            System.out.println("A DLC " + dlc.getNome() + " está sem data de lançamento válida.");
            return;
        }
        System.out.println("O jogo " + nomeJogo + " lançou a DLC " + dlc.getNome()
                + " em " + dlc.getDataLancamento() + ", custando R$" + dlc.getPreco());
    }

    /**
     * Mantido só para não quebrar quem ainda chama este nome (TarefaDLCRR, Main...).
     * Agora vai direto na API, não lê mais o JSON.
     */
    @Deprecated
    public static Produto getJogoComJsonLocal(String appid, long diasSemJogar) {
        return getJogo(appid, diasSemJogar);
    }
}