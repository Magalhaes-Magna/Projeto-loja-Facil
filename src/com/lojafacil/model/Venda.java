package com.lojafacil.model;

import java.util.Date;
import java.util.List;

public class Venda {

    private int id;
    private Date dataVenda;
    private Cliente cliente;
    private List<ItemVenda> itensVenda;

    //construtor padrão sem parâmetros
    public Venda() {
    }

    //Construtor com parâmetros
    public Venda(int id, Date dataVenda, Cliente cliente, List<ItemVenda> itensVenda) {
        this.id = id;
        this.dataVenda = dataVenda;
        this.cliente = cliente;
        this.itensVenda = itensVenda;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemVenda> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<ItemVenda> itensVenda) {
        this.itensVenda = itensVenda;
    }

    public void adicionarItemVenda(ItemVenda itemVenda) {
        this.itensVenda.add(itemVenda);
    }
}
