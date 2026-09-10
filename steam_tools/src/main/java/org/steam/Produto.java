package org.steam;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Produto {
    private String nome = "Sem nome";
    private String appid = "000";
    private double preco = 00.00;
    private long diasSemJogar = 0;
    private List<Produto> dlcs = new ArrayList<>();


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataLancamento = null;


    //construtores


    public Produto() {
    }

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

    public List<Produto> getDlcs() {
        return dlcs;
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

    public void setAppid(String appid) {this.appid = appid; }

    public void setDiasSemJogar(long diasSemJogar) {this.diasSemJogar = diasSemJogar; }




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
