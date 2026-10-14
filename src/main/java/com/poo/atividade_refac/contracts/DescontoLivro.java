package com.poo.atividade_refac.contracts;

public interface DescontoLivro {
    boolean aceita(String categoria);

    double aplicar(double preco);
}
