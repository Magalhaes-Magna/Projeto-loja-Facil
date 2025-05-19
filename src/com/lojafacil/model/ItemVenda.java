package com.lojafacil.model;

public class ItemVenda {

    private int id;
    private Produto produto;
    private int quantidade;
    private double precoVenda;

    // construtor padrão sem parâmetros
    public ItemVenda() {
    }
    //construtor com parâmetros

    public ItemVenda(int id, Produto produto, int quantidade, double precoVenda) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoVenda = precoVenda;
    }
    // Getters e Setters   

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(double precoVenda) {
        this.precoVenda = precoVenda;
    }

    public double calcularSubtotal() {
        return this.quantidade * this.precoVenda;
    }
}
