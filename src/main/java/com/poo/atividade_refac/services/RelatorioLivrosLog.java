package com.poo.atividade_refac.services;

import com.poo.atividade_refac.contracts.RelatorioLivros;
import com.poo.atividade_refac.repositories.LivroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RelatorioLivrosLog implements RelatorioLivros {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelatorioLivrosLog.class);
    private final LivroRepository repository;

    public RelatorioLivrosLog(LivroRepository repository) {
        this.repository = repository;
    }

    @Override
    public void gerar() {
        LOGGER.info("Quantidade de livros: {}", repository.count());
    }
}
