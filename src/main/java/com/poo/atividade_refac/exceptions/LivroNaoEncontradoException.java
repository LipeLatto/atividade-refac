package com.poo.atividade_refac.exceptions;

public class LivroNaoEncontradoException extends RuntimeException {
    public LivroNaoEncontradoException(Long id) {
        super("Livro não encontrado: " + id);
    }
}
