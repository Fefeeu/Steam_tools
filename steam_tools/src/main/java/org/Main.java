package org;


import org.steam.Produto;
import org.steam.controller;

public class Main {
    public static void main(String[] args) throws InterruptedException {


        Produto[] listajogos = controller.MostrarJogos();

        controller t0 = new controller();
        t0.setI(0);
        t0.setListaJogos(listajogos);

        controller t1 = new controller();
        t1.setI(1);
        t1.setListaJogos(listajogos);

        //sem thread
        long tempoInicial = System.currentTimeMillis();

        controller.MostrarJogos();

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
