package org.steam;

import org.steam.api.ProductAPI;
import org.steam.api.UserAPI;
import org.steam.util.JsonExporter;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class controller extends Thread {

    private static final String STEAM_ID = "76561198241059866"; // ideal: vir do ConfigLoader
    private static final int THREADS = 6;

    @Override
    public void run() {
        MostrarJogos();
    }

    /** Busca a biblioteca direto na Steam. Nenhuma leitura do JSON. */
    public static Produto[] MostrarJogos() {
        Produto[] lista = UserAPI.getJogos(STEAM_ID);   // appids + dias sem jogar

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < lista.length; i++) {
            final int idx = i;
            final String appid = lista[i].getAppid();
            final long dias = lista[i].getDiasSemJogar();

            pool.submit(() -> {
                try {
                    Produto p = ProductAPI.getJogo(appid, dias);   // o semáforo limita aqui dentro

                    if (!ProductAPI.nomeValido(p)) {
                        System.out.println("[" + Thread.currentThread().getName() + "] Falhou: " + appid);
                    }
                    lista[idx] = p;   // cada thread escreve só na própria posição: sem conflito
                    System.out.println(p);
                } catch (Exception e) {
                    System.err.println("Erro no jogo " + appid + ": " + e);
                }
            });
        }

        pool.shutdown();
        try {
            pool.awaitTermination(2, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        return lista;
    }

    /** Backup opcional: busca tudo na API e grava no JSON (só escrita). */
    public static void CriarJsonDaSteam() {
        Produto[] lista = MostrarJogos();
        java.util.Arrays.sort(lista, Comparator.comparingLong(Produto::getDiasSemJogar).reversed());

        System.out.println("Salvando dados coletados no JSON local...");
        JsonExporter.salvarListaProdutosComoJson(lista);
    }
}