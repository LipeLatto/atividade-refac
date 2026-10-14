package com.poo.atividade_refac.services;

import com.poo.atividade_refac.contracts.DescontoLivro;

public final class DescontoPorCategoria implements DescontoLivro {
    private final String categoria;
    private final double fator;

    public DescontoPorCategoria(String categoria, double fator) {
        this.categoria = categoria;
        this.fator = fator;
    }

    @Override
    public boolean aceita(String categoria) {
        return this.categoria.equals(categoria);
    }

    @Override
    public double aplicar(double preco) {
        return preco * fator;
    }
}
