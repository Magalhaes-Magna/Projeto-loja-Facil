package com.lojafacil.model;

public class Produto {

    private int id;
    private String nome;
    private String descricao;
    private int quantidadeEstoque;
    private double precoCusto;
    private double precoVenda;
    private int estoqueMinimo;

    //padrão sem parâmetros
    public Produto() {
    }
    //construtor com parâmetros

    public Produto(int id, String nome, String descricao, int quantidadeEstoque, double precoCusto, double precoVenda, int estoqueMinimo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.quantidadeEstoque = quantidadeEstoque;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.estoqueMinimo = estoqueMinimo;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public double getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(double precoCusto) {
        this.precoCusto = precoCusto;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(double precoVenda) {
        this.precoVenda = precoVenda;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public void adicionarQuantidadeEstoque(int quantidade) {
        this.quantidadeEstoque += quantidade;
    }

    public void removerQuantidadeEstoque(int quantidade) {
        this.quantidadeEstoque -= quantidade;
    }

    public boolean verificarEstoqueMinimo() {
        return this.quantidadeEstoque <= this.estoqueMinimo;
    }
}
