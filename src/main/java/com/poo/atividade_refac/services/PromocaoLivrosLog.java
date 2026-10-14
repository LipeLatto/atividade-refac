package com.poo.atividade_refac.services;

import com.poo.atividade_refac.contracts.PromocaoLivros;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PromocaoLivrosLog implements PromocaoLivros {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromocaoLivrosLog.class);

    @Override
    public void enviar() {
        // A aplicação original apenas simulava o envio de email.
        LOGGER.info("Email promocional enviado (simulação)");
    }
}
