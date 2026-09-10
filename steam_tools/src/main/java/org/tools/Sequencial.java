package org.tools;

import org.output.Notificacao;
import org.steam.ComparadorBiblioteca;
import org.steam.MonitorJogadores;
import org.steam.Usuario;
import org.steam.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Sequencial {

    private static final int REPETICOES = 5;

    public static void main(String[] args) throws InterruptedException {
        long inicio = System.currentTimeMillis();

        for (int rodada = 1; rodada <= REPETICOES; rodada++) {
            System.out.println("\n----- Rodada " + rodada + "/" + REPETICOES + " -----");
            notificacaoWishlist();
            comparacaoBiblioteca();
            monitorJogadores();
            controller.MostrarJogos();
        }

        long duracao = System.currentTimeMillis() - inicio;

        System.out.println("\n==================================");
        System.out.println("Tempo total (SEQUENCIAL): " + duracao + "ms");
        System.out.println("==================================");
    }

    private static void notificacaoWishlist() {
        System.out.println("[Wishlist] Verificando lista de desejos...");

        Usuario eu = new Usuario("76561198299393168");
        eu.atualizarListaDeDesejos();
        Notificacao.wishList(eu);
        eu.atualizaBeckupWishList();

        System.out.println("[Wishlist] Concluído.");
    }

    private static void comparacaoBiblioteca() throws InterruptedException {
        System.out.println("[Biblioteca] Comparando bibliotecas...");

        String[] amigos = {
                "76561198299393168",
                "76561199497837378"
        };

        ComparadorBiblioteca comparador = new ComparadorBiblioteca();
        comparador.coletarBibliotecas(amigos);

        Set<String> comuns = comparador.jogosEmComum(amigos);
        Set<String> exclusivosPrimeiro = comparador.jogosExclusivos(amigos[0], amigos[1]);

        comparador.imprimirRelatorio(comuns, exclusivosPrimeiro, amigos[0], amigos);

        System.out.println("[Biblioteca] Concluído.");
    }

    private static void monitorJogadores() throws InterruptedException {
        System.out.println("[Jogadores] Monitorando jogadores online...");

        Map<String, String> jogos = new LinkedHashMap<>();
        jogos.put("730", "Counter-Strike 2");
        jogos.put("570", "Dota 2");
        jogos.put("440", "Team Fortress 2");
        jogos.put("250900", "The Binding of Isaac: Rebirth");

        MonitorJogadores monitor = new MonitorJogadores(jogos);
        monitor.monitorarUmaVez();
        monitor.imprimirRelatorio();

        System.out.println("[Jogadores] Concluído.");
    }
}