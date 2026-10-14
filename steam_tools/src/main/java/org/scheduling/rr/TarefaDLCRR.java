package org.scheduling.rr;

import org.steam.Produto;
import org.steam.api.ProductAPI;
import org.steam.api.UserAPI;

import java.util.Arrays;

/**
 * Buscador de DLCs: 1 passo = processar 1 jogo da biblioteca.
 * Agora lê a lista de jogos direto da Steam (não usa mais o JSON local).
 * As requisições passam pelo semáforo do ApiConnection.
 */
public class TarefaDLCRR implements TarefaRR {

    private final String steamId;
    private final int repeticoes;
    private final int limiteJogos;   // 0 = sem limite
    private Produto[] listaJogos = null;
    private int indice = 0;
    private int rodada = 1;

    public TarefaDLCRR(String steamId, int repeticoes, int limiteJogos) {
        this.steamId = steamId;
        this.repeticoes = repeticoes;
        this.limiteJogos = limiteJogos;
    }

    @Override
    public String getNome() {
        return "DLC";
    }

    @Override
    public boolean executarPasso() {
        if (listaJogos == null) {
            Produto[] todos = UserAPI.getJogos(steamId);   // 1 requisição: appids + dias sem jogar
            if (todos.length == 0) {
                System.out.println("[DLC] Nenhum jogo retornado pela Steam (perfil privado?).");
                return false;
            }
            listaJogos = (limiteJogos > 0 && limiteJogos < todos.length)
                    ? Arrays.copyOf(todos, limiteJogos)
                    : todos;
            indice = 0;
        }

        Produto jogo = listaJogos[indice];
        Produto resultado = ProductAPI.getJogo(jogo.getAppid(), jogo.getDiasSemJogar());

        if (!ProductAPI.nomeValido(resultado)) {
            System.out.println("[DLC] Falhou: " + jogo.getAppid());
        }
        listaJogos[indice] = resultado;
        indice++;

        if (indice == listaJogos.length) {
            System.out.println("[DLC] Rodada " + rodada + "/" + repeticoes + " concluída.");
            if (rodada == repeticoes) {
                return false;
            }
            rodada++;
            indice = 0;
            listaJogos = null;
        }
        return true;
    }
}