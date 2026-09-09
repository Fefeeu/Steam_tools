package org.tools.steam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tools.steam.api.UserAPI;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;

public class Usuario {
    private List<Jogo> wishList = null;
    private List<Jogo> beckupWishList = null;
    private String idUsuario = "000";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path caminhoBackup = null;

    public Usuario(String idUsuario) {
        this.idUsuario = idUsuario;
        this.caminhoBackup = Path.of("backup_wishlist_" + idUsuario + ".json");
    }

    public void atualizarListaDeDesejos() {
        if (this.wishList != null) {
            this.beckupWishList = new ArrayList<>(this.wishList);
        } else {
            this.beckupWishList = getFileBeckupWishList();
        }
        this.wishList = UserAPI.getWishlist(this.idUsuario);
    }

    public void atualizaBeckupWishList(){
        salvarWishListEmArquivo();
    }

    public List<Jogo> getFileBeckupWishList() {
        if (!Files.exists(caminhoBackup)) {
            return new ArrayList<>();
        }

        try {
            String conteudo = Files.readString(caminhoBackup);
            Type tipoLista = new TypeToken<List<Jogo>>() {}.getType();
            List<Jogo> lista = gson.fromJson(conteudo, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Erro ao ler backup: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarWishListEmArquivo() {
        try {
            String json = gson.toJson(this.wishList);
            Files.writeString(caminhoBackup, json);
        } catch (IOException e) {
            System.err.println("Erro ao salvar backup: " + e.getMessage());
        }
    }

    public void escreverListaDeDesejos(){
        for(int i = 0; i < this.wishList.size(); i++){
            System.out.println(this.wishList.get(i));
        }
    }

    public List<Jogo> getWishList() {
        return wishList;
    }
}
