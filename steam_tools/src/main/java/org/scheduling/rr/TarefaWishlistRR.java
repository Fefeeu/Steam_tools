package org.scheduling.rr;

import org.json.JSONArray;
import org.json.JSONObject;
import org.output.Notificacao;
import org.steam.Jogo;
import org.steam.Usuario;
import org.steam.api.UserAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Notificação da lista de desejos dividida em passos de 1 requisição:
 *   - 1º passo de cada verificação: busca os itens da lista de desejos;
 *   - passos seguintes: busca os dados de 1 jogo da lista;
 *   - ao buscar o último jogo: notifica as promoções e salva o backup.
 *
 * Se a Steam recusar uma requisição (limite da API), a tarefa cede a CPU
 * e tenta o mesmo jogo de novo na próxima fatia, em vez de dormir 10s com a CPU.
 */
public class TarefaWishlistRR implements TarefaRR {

    private static final int MAX_TENTATIVAS = 3;

    private final Usuario usuario;
    private final int repeticoes;
    private int verificacao = 0;

    private JSONArray itens = null; // null = verificação ainda não começou
    private List<Jogo> novaLista;
    private int indice;
    private int tentativas;
    private boolean cederCpu = false;

    public TarefaWishlistRR(String steamId, int repeticoes) {
        this.usuario = new Usuario(steamId);
        this.repeticoes = repeticoes;
    }

    @Override
    public String getNome() {
        return "Wishlist";
    }

    @Override
    public boolean executarPasso() {
        cederCpu = false;

        if (itens == null) {
            usuario.prepararBackup();
            itens = UserAPI.getWishListItems(usuario.getIdUsuario());
            novaLista = new ArrayList<>();
            indice = 0;
            tentativas = 0;
            System.out.println("[Wishlist] Verificação " + (verificacao + 1) + "/" + repeticoes
                    + ": " + itens.length() + " jogos na lista.");

            return itens.length() == 0 ? finalizarVerificacao() : true;
        }

        JSONObject item = itens.getJSONObject(indice);
        int appid = item.getInt("appid");
        Jogo jogo = UserAPI.buscarJogoDaWishlist(appid, item.getInt("priority"));

        if (jogo == null) {
            tentativas++;
            if (tentativas < MAX_TENTATIVAS) {
                System.out.println("[Wishlist] Falha no jogo " + appid + " (tentativa " + tentativas + "/"
                        + MAX_TENTATIVAS + "), cedendo a CPU.");
                cederCpu = true;
                return true;
            }
            System.out.println("[Wishlist] Desisti do jogo " + appid + " após " + MAX_TENTATIVAS + " tentativas.");
        } else {
            novaLista.add(jogo);
        }

        indice++;
        tentativas = 0;
        return indice < itens.length() ? true : finalizarVerificacao();
    }

    @Override
    public boolean querCederCpu() {
        return cederCpu;
    }

    private boolean finalizarVerificacao() {
        usuario.definirListaDeDesejos(novaLista);
        Notificacao.wishList(usuario);
        usuario.atualizaBeckupWishList();
        verificacao++;
        itens = null;

        System.out.println("[Wishlist] Verificação " + verificacao + "/" + repeticoes + " concluída.");
        return verificacao < repeticoes;
    }
}
