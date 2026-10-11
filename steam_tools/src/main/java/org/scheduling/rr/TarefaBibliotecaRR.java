package org.scheduling.rr;

import org.steam.api.LibraryAPI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Comparador de bibliotecas: 1 passo = buscar a biblioteca de 1 usuário. */
public class TarefaBibliotecaRR implements TarefaRR {

    private final String[] steamIds;
    private final int repeticoes;
    private final Map<String, Map<String, String>> bibliotecas = new HashMap<>();
    private int passoAtual = 0;

    public TarefaBibliotecaRR(String[] steamIds, int repeticoes) {
        this.steamIds = steamIds;
        this.repeticoes = repeticoes;
    }

    @Override
    public String getNome() {
        return "Biblioteca";
    }

    @Override
    public boolean executarPasso() {
        String id = steamIds[passoAtual % steamIds.length];
        bibliotecas.put(id, LibraryAPI.getOwnedGames(id));
        passoAtual++;

        if (passoAtual % steamIds.length == 0) {
            int rodada = passoAtual / steamIds.length;
            Set<String> comuns = jogosEmComum();
            Set<String> exclusivos = jogosExclusivos();

            if (rodada == repeticoes) {
                imprimirRelatorio(comuns, exclusivos);
            }
            System.out.println("[Biblioteca] Rodada " + rodada + "/" + repeticoes
                    + " - comuns: " + comuns.size() + ", exclusivos: " + exclusivos.size());
        }

        return passoAtual < steamIds.length * repeticoes;
    }

    private Set<String> jogosEmComum() {
        Set<String> comuns = new HashSet<>(bibliotecas.getOrDefault(steamIds[0], Map.of()).keySet());
        for (int i = 1; i < steamIds.length; i++) {
            comuns.retainAll(bibliotecas.getOrDefault(steamIds[i], Map.of()).keySet());
        }
        return comuns;
    }

    private Set<String> jogosExclusivos() {
        Set<String> exclusivos = new HashSet<>(bibliotecas.getOrDefault(steamIds[0], Map.of()).keySet());
        for (int i = 1; i < steamIds.length; i++) {
            exclusivos.removeAll(bibliotecas.getOrDefault(steamIds[i], Map.of()).keySet());
        }
        return exclusivos;
    }

    private String nomeDoJogo(String appid) {
        for (Map<String, String> biblioteca : bibliotecas.values()) {
            if (biblioteca.containsKey(appid)) return biblioteca.get(appid);
        }
        return appid;
    }

    private void imprimirRelatorio(Set<String> comuns, Set<String> exclusivos) {
        System.out.println("\n\n\n");
        String espacos = " ============================= ";

        System.out.println(espacos + "Bibliotecas" + espacos);
        for (String id : steamIds) {
            System.out.println(id + ": " + bibliotecas.getOrDefault(id, Map.of()).size() + " jogos");
        }

        System.out.println("\n\n\n");
        System.out.println(espacos + "Jogos Em Comum (" + comuns.size() + ")" + espacos);
        for (String appid : comuns) {
            System.out.println(nomeDoJogo(appid));
        }

        System.out.println("\n\n\n");
        System.out.println(espacos + "Jogos Exclusivos de " + steamIds[0] + " (" + exclusivos.size() + ")" + espacos);
        for (String appid : exclusivos) {
            System.out.println(nomeDoJogo(appid));
        }
        System.out.println("\n\n\n");
    }
}
