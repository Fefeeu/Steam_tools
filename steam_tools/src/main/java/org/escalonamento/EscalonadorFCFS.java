package org.escalonamento;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Escalonador FCFS (First-Come First-Served).
 *
 * A Ready Queue é uma fila FIFO: o processo que chegou primeiro é o primeiro a usar a CPU,
 * e executa até terminar (não-preemptivo) antes de o próximo ser despachado.
 */
public class EscalonadorFCFS {
    private final Deque<Processo> filaDeProntos = new ArrayDeque<>();
    private final List<Processo> finalizados = new ArrayList<>();
    private final long t0 = System.nanoTime();

    /** Milissegundos desde a criação do escalonador. */
    public long agora() {
        return (System.nanoTime() - t0) / 1_000_000;
    }

    /** Coloca o processo no fim da Ready Queue, marcando o tempo de chegada. */
    public void adicionar(Processo p) {
        p.setChegada(agora());
        filaDeProntos.addLast(p);
        log(p + " chegou na Ready Queue " + filaDeProntos);
    }

    /** Despacha os processos da Ready Queue, um por vez, na ordem de chegada. */
    public void executar() {
        while (!filaDeProntos.isEmpty()) {
            Processo p = filaDeProntos.pollFirst();
            log("despachando " + p + " | Ready Queue: " + filaDeProntos);

            // Os tempos ficam colados na execução para o log não entrar no pico de CPU medido.
            p.setInicio(agora());
            try {
                p.executar(); // não-preemptivo: só sai da CPU quando termina
            } catch (RuntimeException e) {
                log(p + " terminou com erro: " + e.getMessage());
            }
            p.setFim(agora());

            finalizados.add(p);
            log(p + " terminou (esperou " + p.getTempoEspera() + " ms, pico de CPU "
                    + p.getPicoDeCpu() + " ms)");
        }
    }

    public List<Processo> getFinalizados() {
        return finalizados;
    }

    public void imprimirRelatorio() {
        String espacos = " ============================= ";
        System.out.println("\n" + espacos + "Relatório FCFS" + espacos);
        System.out.printf("%-14s %8s %8s %8s %8s %8s %11s%n",
                "Processo", "Chegada", "Início", "Fim", "Pico", "Espera", "Turnaround");

        long somaEspera = 0;
        long somaTurnaround = 0;
        for (Processo p : finalizados) {
            System.out.printf("%-14s %8d %8d %8d %8d %8d %11d%n",
                    p.getNome(), p.getChegada(), p.getInicio(), p.getFim(),
                    p.getPicoDeCpu(), p.getTempoEspera(), p.getTurnaround());
            somaEspera += p.getTempoEspera();
            somaTurnaround += p.getTurnaround();
        }

        if (finalizados.isEmpty()) {
            System.out.println("Nenhum processo executado.");
            return;
        }

        int n = finalizados.size();
        long duracao = finalizados.get(n - 1).getFim();
        System.out.println();
        System.out.println("Ordem de execução:      " + finalizados);
        System.out.printf("Tempo médio de espera:  %.2f ms%n", (double) somaEspera / n);
        System.out.printf("Turnaround médio:       %.2f ms%n", (double) somaTurnaround / n);
        System.out.printf("Throughput:             %.2f processos/s%n", n * 1000.0 / Math.max(duracao, 1));
    }

    private void log(String mensagem) {
        System.out.printf("[%6d ms] [FCFS] %s%n", agora(), mensagem);
    }
}
