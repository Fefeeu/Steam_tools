package org.scheduling.rr;

import org.steam.api.PlayerCountAPI;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Monitor de jogadores online: 1 passo = 1 consulta de 1 jogo. */
public class TarefaJogadoresRR implements TarefaRR {

    private final Map<String, String> jogos;
    private final List<String> appids;
    private final Map<String, Integer> jogadoresOnline = new LinkedHashMap<>();
    private final int repeticoes;
    private int passoAtual = 0;

    public TarefaJogadoresRR(Map<String, String> jogos, int repeticoes) {
        this.jogos = jogos;
        this.appids = new ArrayList<>(jogos.keySet());
        this.repeticoes = repeticoes;
    }

    @Override
    public String getNome() {
        return "Jogadores";
    }

    @Override
    public boolean executarPasso() {
        String appid = appids.get(passoAtual % appids.size());
        jogadoresOnline.put(appid, PlayerCountAPI.getCurrentPlayers(appid));
        passoAtual++;

        if (passoAtual % appids.size() == 0) {
            System.out.println("[Jogadores] Verificação " + (passoAtual / appids.size()) + "/" + repeticoes + " concluída.");
        }

        if (passoAtual >= appids.size() * repeticoes) {
            imprimirRelatorio();
            return false;
        }
        return true;
    }

    private void imprimirRelatorio() {
        System.out.println("\n\n\n");
        String espacos = " ============================= ";
        System.out.println(espacos + "Jogadores Online" + espacos);
        jogadoresOnline.forEach((appid, cont) ->
                System.out.println(jogos.getOrDefault(appid, appid) + "(" + appid + "):" + cont + " jogadores online"));
        System.out.println("\n\n\n");
    }
}
