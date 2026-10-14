package org.scheduling.rr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Escalonador Round Robin sobre threads reais, sincronizado com SEMÁFOROS
 * (no lugar de synchronized / wait / notifyAll).
 *
 * Dois tipos de semáforo:
 *  - turno (um por tarefa, começa em 0): a tarefa fica bloqueada nele até receber a "CPU".
 *    Quem dá a CPU faz turno.release(); a tarefa só anda depois do turno.acquire().
 *  - mutex (binário): exclusão mútua sobre a fila de prontos, as métricas e a troca de contexto.
 *
 * Só a tarefa que recebeu o turno trabalha. Ela executa passos até acabar o quantum,
 * volta para o fim da fila e entrega o turno para a próxima.
 */
public class EscalonadorRR {

    private final long quantumMs;
    private final List<TarefaRR> tarefas = new ArrayList<>();
    private final Deque<TarefaRR> filaProntos = new ArrayDeque<>();
    private final Map<TarefaRR, Metricas> metricas = new LinkedHashMap<>();
    private final Map<TarefaRR, Semaphore> turno = new LinkedHashMap<>();
    private final Semaphore mutex = new Semaphore(1, true);

    private TarefaRR emExecucao = null;
    private int trocasDeContexto = 0;
    private long inicio;

    private static class Metricas {
        long entrouNaFila;     // instante em que entrou na fila de prontos pela última vez
        long tempoEspera = 0;  // soma do tempo esperando na fila
        long turnaround = 0;   // instante de conclusão (todas chegam em t=0)
        int quanta = 0;
        int passos = 0;
    }

    public EscalonadorRR(long quantumMs) {
        this.quantumMs = quantumMs;
    }

    public void adicionar(TarefaRR tarefa) {
        tarefas.add(tarefa);
    }

    public void executar() throws InterruptedException {
        inicio = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();

        mutex.acquireUninterruptibly();
        try {
            for (TarefaRR tarefa : tarefas) {
                Metricas m = new Metricas();
                m.entrouNaFila = inicio;
                metricas.put(tarefa, m);
                turno.put(tarefa, new Semaphore(0));   // ninguém tem a CPU ainda
                filaProntos.addLast(tarefa);
            }
        } finally {
            mutex.release();
        }

        for (TarefaRR tarefa : tarefas) {
            Thread t = new Thread(() -> trabalhar(tarefa), "RR-" + tarefa.getNome());
            threads.add(t);
            t.start();
        }

        log("Iniciando Round Robin com quantum = " + quantumMs + "ms, fila: " + nomesDaFila());

        mutex.acquireUninterruptibly();
        try {
            despacharProxima();   // entrega o primeiro turno
        } finally {
            mutex.release();
        }

        for (Thread t : threads) {
            t.join();
        }

        imprimirMetricas();
    }

    private void trabalhar(TarefaRR tarefa) {
        try {
            boolean temMais = true;
            while (temMais) {
                esperarTurno(tarefa);

                long inicioFatia = System.currentTimeMillis();
                int passos = 0;
                boolean cedeu;
                do {
                    try {
                        temMais = tarefa.executarPasso();
                    } catch (RuntimeException e) {
                        // Sem este catch, uma exceção mataria a thread SEM devolver a CPU
                        // e todas as outras tarefas ficariam bloqueadas para sempre.
                        System.err.println("[RR] Tarefa " + tarefa.getNome() + " falhou e foi encerrada: " + e);
                        temMais = false;
                    }
                    passos++;
                    cedeu = temMais && tarefa.querCederCpu();
                } while (temMais && !cedeu && System.currentTimeMillis() - inicioFatia < quantumMs);

                devolverCpu(tarefa, temMais, cedeu, System.currentTimeMillis() - inicioFatia, passos);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // P(turno): bloqueia até alguém dar a CPU a esta tarefa.
    private void esperarTurno(TarefaRR tarefa) throws InterruptedException {
        turno.get(tarefa).acquire();

        mutex.acquireUninterruptibly();
        try {
            Metricas m = metricas.get(tarefa);
            m.tempoEspera += System.currentTimeMillis() - m.entrouNaFila;
        } finally {
            mutex.release();
        }
    }

    private void devolverCpu(TarefaRR tarefa, boolean temMais, boolean cedeu, long duracao, int passos) {
        mutex.acquireUninterruptibly();
        try {
            Metricas m = metricas.get(tarefa);
            m.quanta++;
            m.passos += passos;

            String destino;
            if (temMais) {
                m.entrouNaFila = System.currentTimeMillis();
                filaProntos.addLast(tarefa);
                destino = cedeu ? "cedeu a CPU, volta p/ fila" : "volta p/ fila";
            } else {
                m.turnaround = System.currentTimeMillis() - inicio;
                destino = "CONCLUÍDA";
            }
            log(tarefa.getNome() + " rodou " + duracao + "ms (" + passos + " passos) -> " + destino);

            despacharProxima();
        } finally {
            mutex.release();
        }
    }

    // Deve ser chamado com o mutex adquirido.
    // V(turno da próxima): libera a próxima tarefa da fila.
    private void despacharProxima() {
        TarefaRR anterior = emExecucao;
        emExecucao = filaProntos.pollFirst();

        if (emExecucao != null) {
            if (anterior != null && emExecucao != anterior) {
                trocasDeContexto++;
            }
            turno.get(emExecucao).release();
        }
    }

    private String nomesDaFila() {
        List<String> nomes = new ArrayList<>();
        for (TarefaRR t : filaProntos) nomes.add(t.getNome());
        return nomes.toString();
    }

    private void log(String mensagem) {
        System.out.println("[RR] t=" + (System.currentTimeMillis() - inicio) + "ms | " + mensagem);
    }

    private void imprimirMetricas() {
        String espacos = " ============================= ";
        System.out.println("\n" + espacos + "Métricas Round Robin (q=" + quantumMs + "ms)" + espacos);
        System.out.printf("%-12s %8s %8s %14s %16s%n", "Tarefa", "Quanta", "Passos", "Espera (ms)", "Turnaround (ms)");

        long somaEspera = 0;
        long somaTurnaround = 0;
        for (Map.Entry<TarefaRR, Metricas> e : metricas.entrySet()) {
            Metricas m = e.getValue();
            System.out.printf("%-12s %8d %8d %14d %16d%n",
                    e.getKey().getNome(), m.quanta, m.passos, m.tempoEspera, m.turnaround);
            somaEspera += m.tempoEspera;
            somaTurnaround += m.turnaround;
        }

        int n = metricas.size();
        System.out.printf("Tempo médio de espera: %.1fms%n", n == 0 ? 0.0 : somaEspera / (double) n);
        System.out.printf("Turnaround médio: %.1fms%n", n == 0 ? 0.0 : somaTurnaround / (double) n);
        System.out.println("Trocas de contexto: " + trocasDeContexto);
    }
}