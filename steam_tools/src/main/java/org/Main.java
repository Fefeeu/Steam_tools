package org;

import org.output.Notificacao;
import org.steam.LoopThread;
import org.steam.Produto;
import org.steam.Usuario;
import org.steam.api.ProductAPI;
import org.steam.api.UserAPI;

import java.util.Arrays;
import java.util.Comparator;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Usuario eu = new Usuario("76561198299393168");

        Thread threadObservador = new Thread(() -> {
            while (true) {
                eu.atualizarListaDeDesejos();
                Notificacao.wishList(eu);
                eu.atualizaBeckupWishList();

                try {
                    Thread.sleep(5); // 5 segundos entre checagens, ajuste como quiser
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        threadObservador.start();
    }
}
