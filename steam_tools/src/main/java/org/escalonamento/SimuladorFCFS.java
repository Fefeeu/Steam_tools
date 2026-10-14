package org.escalonamento;

import org.steam.controller;
import org.tools.Sequencial;

/**
 * Escalona as tarefas do projeto (Wishlist, Biblioteca, Jogadores e DLC) com o algoritmo FCFS.
 */
public class SimuladorFCFS {

    public static void main(String[] args) {
        EscalonadorFCFS fcfs = new EscalonadorFCFS();

        // Os processos chegam na Ready Queue nesta ordem
        fcfs.adicionar(new Processo("Wishlist", Sequencial::notificacaoWishlist));
        fcfs.adicionar(new Processo("Biblioteca", () -> {
            try {
                Sequencial.comparacaoBiblioteca();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        fcfs.adicionar(new Processo("Jogadores", () -> {
            try {
                Sequencial.monitorJogadores();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        fcfs.adicionar(new Processo("DLC", controller::MostrarJogos));

        fcfs.executar(); // CPU despacha em ordem de chegada
        fcfs.imprimirRelatorio();
    }
}
