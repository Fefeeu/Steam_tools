package org;

import org.steam.LoopThread;
import org.steam.Produto;
import org.steam.api.ProductAPI;
import org.steam.api.UserAPI;

import java.util.Arrays;
import java.util.Comparator;


public class Main {
    public static void main(String[] args) throws InterruptedException {

        Produto[] listajogos = UserAPI.getJogos("76561198241059866");

        Arrays.sort(listajogos, Comparator.comparing(Produto::getDiasSemJogar).reversed());

        LoopThread t0 = new LoopThread();
        t0.setI(0);
        t0.setListaJogos(listajogos);

        LoopThread t1 = new LoopThread();
        t1.setI(1);
        t1.setListaJogos(listajogos);

        //sem thread
        long tempoInicial = System.currentTimeMillis();

        for(int i = 0; i < listajogos.length; i ++)
        {
            listajogos[i] = ProductAPI.getJogo(listajogos[i].getAppid(), listajogos[i].getDiasSemJogar());
        }
        long tempoFinal = System.currentTimeMillis();

        float tempoTotal = (tempoFinal - tempoInicial)/1000;

        //Com thread
        tempoInicial = System.currentTimeMillis();

        t0.start();
        t1.start();

        t0.join();
        t1.join();

        tempoFinal = System.currentTimeMillis();

        float tempoTotalThread = (tempoFinal - tempoInicial)/1000;

        System.out.println("Tempo total gasto com uma thread: " + tempoTotal + " s");
        System.out.println("Tempo total gasto em paralelo: " + tempoTotalThread + " s");
    }
}