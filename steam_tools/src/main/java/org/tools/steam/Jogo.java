package org.tools.steam;

public class Jogo {
    private String nome = "";
    private int appid = 0;
    private double preco = 99.99;
    private double promocao = 0.00;
    private boolean em_promocao = false;
    private int prioridade = 999999;

    public Jogo(String nome, int appid, double preco, double promocao){
        this.nome = nome;
        this.appid = appid;
        this.preco = preco;
        this.promocao = promocao;
    }

    public Jogo(String nome, int appid, double preco, double promocao, int prioridade){
        this.nome = nome;
        this.appid = appid;
        this.preco = preco;
        this.promocao = promocao;
        this.prioridade = prioridade;
    }

    public void adicionarPromocao(double promocao){
        this.promocao = promocao;
        this.em_promocao = true;
    }

    public void removerPromocao(){
        this.promocao = 0;
        this.em_promocao = false;
    }

    public void setPrioridade(int prioridade){
        this.prioridade = prioridade;
    }

    @Override
    public String toString() {
        return "Jogo{" +
                "nome='" + nome + '\'' +
                ", appid='" + appid + '\'' +
                ", preco=" + preco +
                ", promocao=" + promocao +
                ", em_promocao=" + em_promocao +
                ", prioridade=" + prioridade +
                '}';
    }
}
