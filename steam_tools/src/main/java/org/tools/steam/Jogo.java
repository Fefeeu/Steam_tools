package org.tools.steam;

public class Jogo {
    private String nome = "";
    private int appid = 0;
    private double preco = 99.99;
    private double promocao = 0.00;
    private boolean emPromocao = false;
    private int prioridade = 999999;
    private boolean foiLancado = false;

    public Jogo(String nome, int appid, double preco, double promocao){
        this.nome = nome;
        this.appid = appid;
        this.preco = preco;
        this.adicionarPromocao(promocao);


    }

    public Jogo(String nome, int appid, double preco, double promocao, int prioridade){
        this.nome = nome;
        this.appid = appid;
        this.preco = preco;
        this.adicionarPromocao(promocao);
        this.prioridade = prioridade;
    }

    public void adicionarPromocao(double promocao){
        this.promocao = promocao;
        if (promocao > 0) this.emPromocao = true;
    }

    public void removerPromocao(){
        this.promocao = 0;
        this.emPromocao = false;
    }

    public void setPrioridade(int prioridade){
        this.prioridade = prioridade;
    }

    public void lancarJogo(){
        this.foiLancado = true;
    }

    public boolean isFoiLancado() {
        return foiLancado;
    }

    public boolean isEmPromocao() {
        return emPromocao;
    }

    public double getPreco() {
        return preco;
    }

    public double getPromocao() {
        return promocao;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return "Jogo{" +
                "nome='" + nome + '\'' +
                ", appid='" + appid + '\'' +
                ", preco=" + preco +
                ", promocao=" + promocao +
                ", em_promocao=" + emPromocao +
                ", prioridade=" + prioridade +
                ", foiLancado?=" + foiLancado +
                '}';
    }
}
