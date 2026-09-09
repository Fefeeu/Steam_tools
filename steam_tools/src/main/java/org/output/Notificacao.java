package org.output;

import org.steam.Jogo;
import org.steam.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Notificacao {

    public static void wishList(Usuario usuario){
        List<Jogo> jogosEmPromocao = getPromotionDiference(usuario);

        System.out.println("\n\n\n");
        String espacos = " ============================= ";
        System.out.println(espacos + "Jogos Em Promoção" + espacos);

        for(int i = 0; i < jogosEmPromocao.size(); i++){
            if (jogosEmPromocao.get(i).isNovaPromocao()) {
                System.out.println(jogosEmPromocao.get(i).getNome() + " == nova ==");
            } else {
                System.out.println(jogosEmPromocao.get(i).getNome());
            }
        }
        System.out.println("\n\n\n");
    }

    public static List<Jogo> getPromotionDiference(Usuario usuario) {
        List<Jogo> wishList = usuario.getWishList();
        List<Jogo> beckupWishList = usuario.getFileBeckupWishList();

        Map<Integer, Jogo> mapaBackup = beckupWishList.stream()
                .collect(Collectors.toMap(Jogo::getAppid, jogo -> jogo));

        List<Jogo> jogosEmPromocao = new ArrayList<>();

        for (Jogo jogo : wishList) {
            Jogo jogoAntigo = mapaBackup.get(jogo.getAppid());
            double promocaoAntiga = (jogoAntigo != null) ? jogoAntigo.getPromocao() : 0;

            if (jogo.getPromocao() > 0 && jogo.getPromocao() != promocaoAntiga) {
                jogo.adicionarPromocao(jogo.getPromocao());
                jogo.setNovaPromocao(true);
            } else if (jogo.getPromocao() <= 0 && promocaoAntiga > 0) {
                jogo.removerPromocao();
            }

            if (jogo.isEmPromocao()) {
                jogosEmPromocao.add(jogo);
            }
        }

        return jogosEmPromocao;
    }

    // outra funcao para verificar se um jogo foi lançado

    // funcao para ver se esta acabando a promocao (tempo)
}
