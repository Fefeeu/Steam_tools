package org.scheduling.rr;

import org.steam.Produto;
import org.steam.api.ProductAPI;
import org.steam.api.UserAPI;

/** Buscador de DLCs: 1 passo = processar 1 jogo da biblioteca local. */
public class TarefaDLCRR implements TarefaRR {

    private final int repeticoes;
    private Produto[] listaJogos = null;
    private int indice = 0;
    private int rodada = 1;

    public TarefaDLCRR(int repeticoes) {
        this.repeticoes = repeticoes;
    }

    @Override
    public String getNome() {
        return "DLC";
    }

    @Override
    public boolean executarPasso() {
        if (listaJogos == null) {
            listaJogos = UserAPI.getJogosDoJsonLocal();
            if (listaJogos.length == 0) {
                System.out.println("[DLC] Nenhum jogo no JSON local.");
                return false;
            }
        }

        Produto jogo = listaJogos[indice];
        listaJogos[indice] = ProductAPI.getJogoComJsonLocal(jogo.getAppid(), jogo.getDiasSemJogar());
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
