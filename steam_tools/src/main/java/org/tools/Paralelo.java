package org.tools;

import org.output.Notificacao;
import org.steam.ComparadorBiblioteca;
import org.steam.MonitorJogadores;
import org.steam.Usuario;
import org.steam.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Paralelo {

    private static final int REPETICOES = 5;

    public static void main(String[] args) throws InterruptedException {
        long inicio = System.currentTimeMillis();

        Thread threadWishlist = new Thread(Paralelo::notificacaoWishlist, "Thread-Wishlist");
        Thread threadBiblioteca = new Thread(Paralelo::comparacaoBiblioteca, "Thread-Biblioteca");
        Thread threadJogadores = new Thread(Paralelo::monitorJogadores, "Thread-Jogadores");
        controller threadDLC = new controller();

        threadDLC.start();
        threadWishlist.start();
        threadBiblioteca.start();
        threadJogadores.start();

        threadDLC.join();
        threadWishlist.join();
        threadBiblioteca.join();
        threadJogadores.join();

        long duracao = System.currentTimeMillis() - inicio;

        System.out.println("\n==================================");
        System.out.println("Tempo total (PARALELO): " + duracao + "ms");
        System.out.println("==================================");
    }

    private static void notificacaoWishlist() {
        System.out.println("[Wishlist] Verificando lista de desejos (" + REPETICOES + "x)... (thread: "
                + Thread.currentThread().getName() + ")");

        Usuario eu = new Usuario("76561198299393168");

        for (int i = 1; i <= REPETICOES; i++) {
            eu.atualizarListaDeDesejos();
            Notificacao.wishList(eu);
            eu.atualizaBeckupWishList();
            System.out.println("[Wishlist] Verificação " + i + "/" + REPETICOES + " concluída.");
        }

        System.out.println("[Wishlist] Concluído.");
    }

    private static void comparacaoBiblioteca() {
        System.out.println("[Biblioteca] Comparando bibliotecas (" + REPETICOES + "x)... (thread: "
                + Thread.currentThread().getName() + ")");

        String[] amigos = {
                "76561198299393168",
                "76561199497837378"
        };

        ComparadorBiblioteca comparador = new ComparadorBiblioteca();

        try {
            for (int i = 1; i <= REPETICOES; i++) {
                comparador.coletarBibliotecas(amigos);

                Set<String> comuns = comparador.jogosEmComum(amigos);
                Set<String> exclusivosPrimeiro = comparador.jogosExclusivos(amigos[0], amigos[1]);

                if (i == REPETICOES) {
                    comparador.imprimirRelatorio(comuns, exclusivosPrimeiro, amigos[0], amigos);
                }

                System.out.println("[Biblioteca] Rodada " + i + "/" + REPETICOES
                        + " - comuns: " + comuns.size() + ", exclusivos: " + exclusivosPrimeiro.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("[Biblioteca] Concluído.");
    }

    private static void monitorJogadores() {
        System.out.println("[Jogadores] Monitorando jogadores online (" + REPETICOES + "x)... (thread: "
                + Thread.currentThread().getName() + ")");

        Map<String, String> jogos = new LinkedHashMap<>();
        jogos.put("730", "Counter-Strike 2");
        jogos.put("570", "Dota 2");
        jogos.put("440", "Team Fortress 2");
        jogos.put("250900", "The Binding of Isaac: Rebirth");

        MonitorJogadores monitor = new MonitorJogadores(jogos);

        try {
            for (int i = 1; i <= REPETICOES; i++) {
                monitor.monitorarUmaVez();
                System.out.println("[Jogadores] Verificação " + i + "/" + REPETICOES + " concluída.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        monitor.imprimirRelatorio();
        System.out.println("[Jogadores] Concluído.");
    }
}