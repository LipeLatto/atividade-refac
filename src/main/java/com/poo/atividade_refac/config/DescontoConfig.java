package com.poo.atividade_refac.config;

import com.poo.atividade_refac.contracts.DescontoLivro;
import com.poo.atividade_refac.services.DescontoPorCategoria;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DescontoConfig {
    @Bean
    DescontoLivro descontoRomance() {
        return new DescontoPorCategoria("ROMANCE", 0.9);
    }

    @Bean
    DescontoLivro descontoTecnologia() {
        return new DescontoPorCategoria("TECNOLOGIA", 0.8);
    }

    @Bean
    DescontoLivro descontoInfantil() {
        return new DescontoPorCategoria("INFANTIL", 0.7);
    }

    @Bean
    DescontoLivro descontoHq() {
        return new DescontoPorCategoria("HQ", 0.6);
    }
}
