package org.steam;

import org.steam.api.PlayerCountAPI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorJogadores {
    private final Map<String, String> nomesJogos;
    private final Map<String, Integer> jogadoresOnline = new ConcurrentHashMap<>();

    public MonitorJogadores(Map<String, String> nomesJogos) {
        this.nomesJogos = nomesJogos;
    }

    public void monitorarUmaVez() throws InterruptedException {
        Thread[] threads = new Thread[nomesJogos.size()];
        int i = 0;

        for (String appid : nomesJogos.keySet()) {
            Thread t =  new Thread(() -> {
                int cont = PlayerCountAPI.getCurrentPlayers(appid);
                jogadoresOnline.put(appid, cont);
            }, "Monitor-" + appid);
            threads[i++] = t;
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }
    public void imprimirRelatorio(){
        System.out.println("---- JOGADORES ONLINE ----");
        jogadoresOnline.forEach((appid, cont) -> {
            String nome = nomesJogos.getOrDefault(appid, appid);
            System.out.println(nome + "(" + appid + "):" + cont + " jogadores online");
        });
    }
}
