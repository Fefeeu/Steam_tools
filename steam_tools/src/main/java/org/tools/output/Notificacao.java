package org.tools.output;

import org.tools.steam.Jogo;

import java.util.ArrayList;
import java.util.List;

public abstract class Notificacao {

    public static void wishList(List<Jogo> wishList){
        List<Jogo> jogosEmPromocao = new ArrayList<>();
        for(int i = 0; i < wishList.size(); i++){
            Jogo jogo = wishList.get(i);
            if(jogo.isEmPromocao() && jogo.isFoiLancado()){
                jogosEmPromocao.add(jogo);
            }
        }

        String espacos = " ============================= ";
        System.out.println(espacos + "Jogos Em Promoção" + espacos);

        for(int i = 0; i < jogosEmPromocao.size(); i++){
            System.out.println(jogosEmPromocao.get(i).getNome());
        }
    }

    public static List<Jogo> getPromotionDiference(List<Jogo> wishListBeckup, List<Jogo> wishList){
        // criar uma função que tenha um for comparando cada objeto, e vendo quais jogos possuem novaPromocao = True, assim destacando os que tem novas promoçoes
        // e uma função que ferifique se saiu da promoção para tirar da lista, precisa?
    }
    // outra funcao para verificar se um jogo foi lançado

    // funcao para ver se esta acabando a promocao (tempo)
}
