package org.tools;

import org.scheduling.rr.EscalonadorRR;
import org.scheduling.rr.TarefaBibliotecaRR;
import org.scheduling.rr.TarefaDLCRR;
import org.scheduling.rr.TarefaJogadoresRR;
import org.scheduling.rr.TarefaWishlistRR;

import java.util.LinkedHashMap;
import java.util.Map;

public class RoundRobin {

    private static final int REPETICOES = 5;
    private static final long QUANTUM_PADRAO_MS = 1000;

    public static void main(String[] args) throws InterruptedException {
        long quantum = args.length > 0 ? Long.parseLong(args[0]) : QUANTUM_PADRAO_MS;

        String[] amigos = {
                "76561198299393168",
                "76561199497837378"
        };

        Map<String, String> jogos = new LinkedHashMap<>();
        jogos.put("730", "Counter-Strike 2");
        jogos.put("570", "Dota 2");
        jogos.put("440", "Team Fortress 2");
        jogos.put("250900", "The Binding of Isaac: Rebirth");

        EscalonadorRR escalonador = new EscalonadorRR(quantum);
        escalonador.adicionar(new TarefaWishlistRR("76561198299393168", REPETICOES));
        escalonador.adicionar(new TarefaBibliotecaRR(amigos, REPETICOES));
        escalonador.adicionar(new TarefaJogadoresRR(jogos, REPETICOES));
        escalonador.adicionar(new TarefaDLCRR(REPETICOES));

        long inicio = System.currentTimeMillis();
        escalonador.executar();
        long duracao = System.currentTimeMillis() - inicio;

        System.out.println("\n==================================");
        System.out.println("Tempo total (ROUND ROBIN, q=" + quantum + "ms): " + duracao + "ms");
        System.out.println("==================================");
    }
}
