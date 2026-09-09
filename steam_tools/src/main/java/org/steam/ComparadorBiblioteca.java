package org.steam;

import org.steam.api.LibraryAPI;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ComparadorBiblioteca {
    private final Map<String, Map<String, String>> bibliotecas = new ConcurrentHashMap<>();

    public void coletarBibliotecas(String[] steamIds) throws InterruptedException {
        Thread[] threads = new Thread[steamIds.length];
        for (int i = 0; i < steamIds.length; i++) {
            String id = steamIds[i];
            threads[i] = new Thread(() -> {
                Map<String, String> jogos = LibraryAPI.getOwnedGames((id));
                bibliotecas.put(id, jogos);
            }, "Biblioteca-" + id);
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    public Set<String> jogosEmComum(String... steamIds) {
        Set<String> comuns = null;
        for (String id : steamIds) {
            Set<String> appids = bibliotecas.getOrDefault(id, Map.of()).keySet();
            comuns = (comuns == null) ? new HashSet<>(appids) : intersecta(comuns, appids);
        }
        return comuns == null ? Set.of() : comuns;
    }

    private Set<String> intersecta(Set<String> a, Set<String> b) {
        Set<String> resultado = new HashSet<>(a);
        resultado.retainAll(b); // mantém só o que existe nos dois (interseção real)
        return resultado;
    }

    public Set<String> jogosExclusivos(String steamId, String... outros) {
        Set<String> exclusivos = new HashSet<>(bibliotecas.getOrDefault(steamId, Map.of()).keySet());
        for (String outroId : outros) {
            exclusivos.removeAll(bibliotecas.getOrDefault(outroId, Map.of()).keySet());
        }
        return exclusivos;
    }

    public String nomeDoJogo(String appid) {
        for (Map<String, String> biblioteca : bibliotecas.values()) {
            if (biblioteca.containsKey(appid)) return biblioteca.get(appid);
        }
        return appid;
    }
    public int quantidadeDeJogos(String steamId) {
        return bibliotecas.getOrDefault(steamId, Map.of()).size();
    }

    public void imprimirRelatorio(Set<String> comuns, Set<String> exclusivos, String steamId, String... todosOsIds) {
        System.out.println("\n\n\n");
        String espacos = " ============================= ";

        System.out.println(espacos + "Bibliotecas" + espacos);
        for (String id : todosOsIds) {
            System.out.println(id + ": " + quantidadeDeJogos(id) + " jogos");
        }

        System.out.println("\n\n\n");
        System.out.println(espacos + "Jogos Em Comum (" + comuns.size() + ")" + espacos);
        for (String appid : comuns) {
            System.out.println(nomeDoJogo(appid));
        }

        System.out.println("\n\n\n");
        System.out.println(espacos + "Jogos Exclusivos de " + steamId + " (" + exclusivos.size() + ")" + espacos);
        for (String appid : exclusivos) {
            System.out.println(nomeDoJogo(appid));
        }
        System.out.println("\n\n\n");
    }
}