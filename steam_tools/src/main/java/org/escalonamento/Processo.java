package org.escalonamento;

/**
 * Um "processo" da simulação: um nome e a tarefa que ele executa quando ganha a CPU.
 * Os tempos são em ms, contados a partir do início da simulação (ver FilaDeProntos.agora()).
 */
public class Processo {
    private final String nome;
    private final Runnable tarefa;

    private long chegada = -1; // quando entrou na Ready Queue
    private long inicio = -1;  // quando ganhou a CPU
    private long fim = -1;     // quando terminou

    public Processo(String nome, Runnable tarefa) {
        this.nome = nome;
        this.tarefa = tarefa;
    }

    void executar() {
        tarefa.run();
    }

    public String getNome() {
        return nome;
    }

    public long getChegada() {
        return chegada;
    }

    public long getInicio() {
        return inicio;
    }

    public long getFim() {
        return fim;
    }

    void setChegada(long chegada) {
        this.chegada = chegada;
    }

    void setInicio(long inicio) {
        this.inicio = inicio;
    }

    void setFim(long fim) {
        this.fim = fim;
    }

    /** Tempo de espera na Ready Queue. No FCFS (não-preemptivo) o processo espera uma única vez. */
    public long getTempoEspera() {
        return inicio - chegada;
    }

    /** Tempo entre a submissão e o término do processo. */
    public long getTurnaround() {
        return fim - chegada;
    }

    /** Pico de CPU medido (quanto tempo o processo ficou executando). */
    public long getPicoDeCpu() {
        return fim - inicio;
    }

    @Override
    public String toString() {
        return nome;
    }
}
