package com.lojafacil.dao;

/**
 * Exceção lançada quando se tenta excluir um cliente que possui vendas
 * associadas.
 */
public class ClienteComVendasException extends Exception {

    public ClienteComVendasException(String message) {
        super(message);
    }

    public ClienteComVendasException(String message, Throwable cause) {
        super(message, cause);
    }
}
