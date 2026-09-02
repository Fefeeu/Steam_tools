package org.steam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Produto {
    private String nome = "";
    private String appid = "000";
    private double preco = 99.99;
    private List<Produto> dlcs = new ArrayList<>();
    private LocalDate dataLancamento = null;
    private long diasSemJogar = 0;

    //construtores
    public Produto(String nome, String appid, double preco, LocalDate dataLancamento, List<Produto> dlcs){
        this.nome = nome;
        this.appid = appid;
        this.preco = preco;
        this.dataLancamento = dataLancamento;
        this.dlcs = dlcs;
    }

    public Produto(String appid, long diasSemJogar)
    {
        this.appid = appid;
        this.diasSemJogar = diasSemJogar;
    }


    //gets
    public String getAppid() {
        return appid;
    }

    public long getDiasSemJogar() {
        return diasSemJogar;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    //sets
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setDlcs(List<Produto> dlcs) {
        this.dlcs = dlcs;
    }

    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }



    @Override
    public String toString() {
        return "Jogo{" +
                "nome='" + nome + '\'' +
                ", appid='" + appid + '\'' +
                ", preco=" + preco +
                ", dlcs=" + dlcs +
                ", lancamento= " + dataLancamento +
                ",dias desda ultima vez jogada = " + diasSemJogar +
                '}';
    }
}
