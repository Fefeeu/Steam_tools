package org.tools;

import org.tools.output.Notificacao;
import org.tools.steam.Usuario;

public class Main {
    static void main() {
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
