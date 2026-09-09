package org.steam;

import org.steam.api.ProductAPI;


public class LoopThread extends Thread{
    private int i = 0;
    private Produto[] listaJogos;

    public void setI(int i) {
        this.i = i;
    }

    public void setListaJogos(Produto[] listaJogos) {
        this.listaJogos = listaJogos;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        for(int i = this.i; i < this.listaJogos.length; i = i+2)
        {
            this.listaJogos[i] = ProductAPI.getJogo(this.listaJogos[i].getAppid(), this.listaJogos[i].getDiasSemJogar());
            System.out.println(name);
        }
    }
}
