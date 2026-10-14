package org.scheduling.rr;

/**
 * Tarefa escalonável pelo Round Robin.
 * O trabalho é dividido em passos: o escalonador só verifica o quantum
 * entre um passo e outro (preempção cooperativa).
 */
public interface TarefaRR {

    String getNome();

    /**
     * Executa uma unidade de trabalho.
     * @return true se ainda falta trabalho, false se a tarefa terminou.
     */
    boolean executarPasso();

    /**
     * Permite que a tarefa ceda a CPU antes do fim do quantum
     * (ex.: requisição falhou por limite da API e é melhor esperar na fila).
     */
    default boolean querCederCpu() {
        return false;
    }
}
