package org.steam;

import org.steam.api.ProductAPI;
import org.steam.api.UserAPI;
import org.steam.util.JsonExporter;

import java.util.Arrays;
import java.util.Comparator;

public class controller extends Thread{
    //


    @Override
    public void run() {
        Produto[] listajogos = UserAPI.getJogosDoJsonLocal();

        for (int i = 0; i < listajogos.length; i++) {
            listajogos[i] = ProductAPI.getJogoComJsonLocal(listajogos[i].getAppid(), listajogos[i].getDiasSemJogar());
        }
    }

    public static void CriarJsonDaSteam()
    {
        Produto[] listajogos = UserAPI.getJogos("76561198241059866");
        Arrays.sort(listajogos, Comparator.comparing(Produto::getDiasSemJogar).reversed());

        for(int i = 0; i < listajogos.length; i++) {
            listajogos[i] = ProductAPI.getJogo(listajogos[i].getAppid(), listajogos[i].getDiasSemJogar());
        }

        System.out.println("Salvando dados coletados no JSON local...");
        JsonExporter.salvarListaProdutosComoJson(listajogos);
    }

    public static Produto[] MostrarJogos()
    {
        Produto[] listajogos = UserAPI.getJogosDoJsonLocal();

        for (int i = 0; i < listajogos.length; i++) {
            listajogos[i] = ProductAPI.getJogoComJsonLocal(listajogos[i].getAppid(), listajogos[i].getDiasSemJogar());
        }
        return listajogos;
    }

}
