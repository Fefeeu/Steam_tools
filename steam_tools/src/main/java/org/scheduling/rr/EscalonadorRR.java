package org.scheduling.rr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Escalonador Round Robin sobre threads reais.
 *
 * Cada tarefa roda na sua própria thread, mas só a tarefa que está com a "CPU"
 * (emExecucao) pode trabalhar. Ela executa passos até acabar o quantum e então
 * devolve a CPU, voltando para o fim da fila de prontos.
 *
 * A troca de turno é feita com o monitor do Java (synchronized / wait / notifyAll).
 */
public class EscalonadorRR {

    private final long quantumMs;
    private final List<TarefaRR> tarefas = new ArrayList<>();
    private final Deque<TarefaRR> filaProntos = new ArrayDeque<>();
    private final Object monitor = new Object();
    private final Map<TarefaRR, Metricas> metricas = new LinkedHashMap<>();

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

        synchronized (monitor) {
            for (TarefaRR tarefa : tarefas) {
                Metricas m = new Metricas();
                m.entrouNaFila = inicio;
                metricas.put(tarefa, m);
                filaProntos.addLast(tarefa);
            }
        }

        for (TarefaRR tarefa : tarefas) {
            Thread t = new Thread(() -> trabalhar(tarefa), "RR-" + tarefa.getNome());
            threads.add(t);
            t.start();
        }

        log("Iniciando Round Robin com quantum = " + quantumMs + "ms, fila: " + nomesDaFila());
        synchronized (monitor) {
            despacharProxima();
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
                boolean cedeu = false;
                do {
                    temMais = tarefa.executarPasso();
                    passos++;
                    cedeu = temMais && tarefa.querCederCpu();
                } while (temMais && !cedeu && System.currentTimeMillis() - inicioFatia < quantumMs);

                devolverCpu(tarefa, temMais, cedeu, System.currentTimeMillis() - inicioFatia, passos);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void esperarTurno(TarefaRR tarefa) throws InterruptedException {
        synchronized (monitor) {
            while (emExecucao != tarefa) {
                monitor.wait();
            }
            Metricas m = metricas.get(tarefa);
            m.tempoEspera += System.currentTimeMillis() - m.entrouNaFila;
        }
    }

    private void devolverCpu(TarefaRR tarefa, boolean temMais, boolean cedeu, long duracao, int passos) {
        synchronized (monitor) {
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
        }
    }

    // Deve ser chamado com o monitor travado.
    private void despacharProxima() {
        TarefaRR anterior = emExecucao;
        emExecucao = filaProntos.pollFirst();
        if (emExecucao != null && anterior != null && emExecucao != anterior) {
            trocasDeContexto++;
        }
        monitor.notifyAll();
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
