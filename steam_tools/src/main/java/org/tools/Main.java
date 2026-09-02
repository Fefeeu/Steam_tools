package org.tools;

import org.tools.output.Notificacao;
import org.tools.steam.Usuario;
import org.tools.steam.api.GameAPI;
import org.tools.steam.api.UserAPI;

public class Main {
    static void main() {
        Usuario eu = new Usuario("76561198299393168");
        eu.atualizarListaDeDesejos();
        Notificacao.wishList(eu.getWishList());
    }
}
