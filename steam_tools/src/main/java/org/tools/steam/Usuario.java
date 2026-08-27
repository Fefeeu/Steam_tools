package org.tools.steam;

import org.tools.steam.api.UserAPI;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private List<Jogo> wishList = new ArrayList<>();
    private String idUsuario = "000";

    public Usuario(String idUsuario){
        this.idUsuario = idUsuario;
    }


    public void atualizarListaDeDesejos(){
        this.wishList = UserAPI.getWishlist(this.idUsuario);
    }

    public void escreverListaDeDesejos(){
        for(int i = 0; i < this.wishList.size(); i++){
            System.out.println(i);
            System.out.println(this.wishList.get(i));
        }
    }
}
