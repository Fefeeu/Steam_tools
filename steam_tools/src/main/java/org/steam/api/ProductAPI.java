package org.steam.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.steam.Produto;
import org.steam.util.JsonExporter;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ProductAPI {


    private static final List<DateTimeFormatter> FORMATADORES = Arrays.asList(
            DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("d/MMM/yyyy", new Locale("pt", "BR")),
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR")) // Adicionado para segurança
    );


    public static Produto getJogo(String appid, long diasSemJogar)
    {
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

            //pegando nome
            jogo.setNome(data.optString("name", null));


            //pegando data de lancamento
            JSONObject releaseDate = data.optJSONObject("release_date");
            if (releaseDate != null) {
                String lancamentoTexto = releaseDate.optString("date", "");
                jogo.setDataLancamento(converterData(lancamentoTexto));
            }


            //pegando preco
            JSONObject priceOverview = data.optJSONObject("price_overview");
            if (priceOverview != null) {
                jogo.setPreco(priceOverview.optDouble("final", 0.0) / 100.0);
            } else {
                jogo.setPreco(0.0);
            }

            //pegando dlcs
            JSONArray dlcsArray = data.optJSONArray("dlc");
            if (dlcsArray != null) {
                List<Produto> dlcs = new ArrayList<>();
                for (int i = 0; i < dlcsArray.length(); i++) {
                    String dlcId = dlcsArray.get(i).toString();

                    Produto dlcJson = getJogo(dlcId, 0);

                    if (dlcJson.getNome() != null && !dlcJson.getNome().isEmpty()) {
                        compDLC(dlcJson, jogo.getNome());
                    }
                    dlcs.add(dlcJson);
                }
                jogo.setDlcs(dlcs);
            } else {
                jogo.setDlcs(Collections.emptyList());
            }

            return jogo;

        }catch (JSONException e) {
            System.err.println("Erro ao processar JSON (Limite de requisições ou formato inválido): " + e.getMessage());
        }

        return jogo;
    }

    private static LocalDate converterData(String lancamentoTexto) {
        if (lancamentoTexto == null || lancamentoTexto.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatador : FORMATADORES) {
            try {
                return LocalDate.parse(lancamentoTexto, formatador);
            } catch (DateTimeParseException ignored) {
                // Continua tentando os outros formatadores
            }
        }
        System.out.println("Não foi possível converter a data: " + lancamentoTexto);
        return null;
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

    public static Produto getJogoComJsonLocal(String appid, long diasSemJogar) {
        Produto[] jogosSalvos = JsonExporter.lerListaProdutosDoJson();

        Map<String, Produto> mapaLocal = new HashMap<>();
        for (Produto p : jogosSalvos) {
            mapaLocal.put(p.getAppid(), p);
        }

        Produto jogoResultado;

        if (mapaLocal.containsKey(appid)) {
            jogoResultado = mapaLocal.get(appid);
            jogoResultado.setDiasSemJogar(diasSemJogar); // Atualiza com os dias enviados por parâmetro
        } else {
            jogoResultado = getJogo(appid, diasSemJogar);

            if (jogoResultado.getNome() != null && !jogoResultado.getNome().isEmpty()) {
                mapaLocal.put(appid, jogoResultado);
                Produto[] listaAtualizada = mapaLocal.values().toArray(new Produto[0]);
                JsonExporter.salvarListaProdutosComoJson(listaAtualizada);
            }
        }

        if (jogoResultado.getNome() != null && !jogoResultado.getNome().isEmpty()) {
            System.out.println("----------------------------------------");
            System.out.println("Jogo: " + jogoResultado.getNome() + " (AppID: " + jogoResultado.getAppid() + ")");
            System.out.println("Preço: R$ " + jogoResultado.getPreco());
            System.out.println("Última vez jogado: há " + jogoResultado.getDiasSemJogar() + " dias");

            // Verificando se o jogo possui DLCs mapeadas
            if (jogoResultado.getDlcs() != null && !jogoResultado.getDlcs().isEmpty()) {
                System.out.println("  ↳ Contém " + jogoResultado.getDlcs().size() + " DLC(s):");
                for (Produto dlc : jogoResultado.getDlcs()) {
                    System.out.println("    - " + dlc.getNome() + " | Lançamento: " + dlc.getDataLancamento());
                }
            }
        }

        try {
            Thread.sleep(100); // 1500 milissegundos = 1.5 segundos de espera
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return jogoResultado;
    }


}
