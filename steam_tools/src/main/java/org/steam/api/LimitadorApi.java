package org.steam.util;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Semáforos que controlam quantas threads falam com a Steam ao mesmo tempo.
 * Cada permissão fica ocupada durante a pausa, o que define o ritmo das requisições.
 *
 * taxa aproximada = permissões / pausa
 *   STORE:     3 / 5s = ~36 requisições por minuto
 *   STEAM_API: 2 / 1s = ~120 requisições por minuto
 */
public enum LimitadorApi {

    // store.steampowered.com/api/appdetails (ProductAPI e GameAPI): limite apertado
    STORE(3, 5000),

    // api.steampowered.com (biblioteca, wishlist, jogadores online): mais folgado
    STEAM_API(2, 1000);

    private final Semaphore semaforo;
    private final long pausaMs;

    LimitadorApi(int permissoes, long pausaMs) {
        this.semaforo = new Semaphore(permissoes, true); // true = fila justa (FIFO)
        this.pausaMs = pausaMs;
    }

    /** Executa a requisição segurando uma permissão. Devolve null se a thread for interrompida. */
    public <T> T executar(Supplier<T> requisicao) {
        try {
            semaforo.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        try {
            System.out.println("[" + name() + "] " + Thread.currentThread().getName()
                    + " entrou | livres: " + semaforo.availablePermits());
            return requisicao.get();
        } finally {
            try {
                Thread.sleep(pausaMs);   // a permissão continua ocupada durante a pausa
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            semaforo.release();          // sempre liberada
        }
    }
}
