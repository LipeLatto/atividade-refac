package com.poo.atividade_refac.services;

import com.poo.atividade_refac.contracts.DescontoLivro;
import com.poo.atividade_refac.contracts.PromocaoLivros;
import com.poo.atividade_refac.contracts.RelatorioLivros;
import com.poo.atividade_refac.entities.Livro;
import com.poo.atividade_refac.exceptions.LivroNaoEncontradoException;
import com.poo.atividade_refac.exceptions.RegraNegocioException;
import com.poo.atividade_refac.repositories.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LivroServiceTests {
    private LivroRepository repository;
    private RelatorioLivros relatorio;
    private PromocaoLivros promocao;
    private LivroService service;

    @BeforeEach
    void preparar() {
        repository = mock(LivroRepository.class);
        relatorio = mock(RelatorioLivros.class);
        promocao = mock(PromocaoLivros.class);
        service = new LivroService(repository, List.of(), relatorio, promocao);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejeitaTituloInvalidoSemPersistir(String titulo) {
        Livro livro = livro("OUTRA", 100.0);
        livro.setTitulo(titulo);
        assertThrows(RegraNegocioException.class, () -> service.cadastrar(livro));
        verifyNoInteractions(repository, relatorio, promocao);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejeitaPrecoInvalidoSemPersistir(Double preco) {
        assertThrows(RegraNegocioException.class, () -> service.cadastrar(livro("OUTRA", preco)));
        verifyNoInteractions(repository, relatorio, promocao);
    }

    @Test
    void aceitaNovaEstrategiaSemAlterarServico() {
        DescontoLivro desconto = new DescontoPorCategoria("NOVA", 0.5);
        service = new LivroService(repository, List.of(desconto), relatorio, promocao);
        Livro entrada = livro("NOVA", 100.0);
        Livro salvo = new Livro(1L, "Livro", "Autor", 50.0, "NOVA");
        when(repository.save(entrada)).thenReturn(salvo);

        assertSame(salvo, service.cadastrar(entrada));
        assertEquals(50.0, entrada.getPreco(), 0.00001);
        var ordem = inOrder(repository, relatorio, promocao);
        ordem.verify(repository).save(entrada);
        ordem.verify(relatorio).gerar();
        ordem.verify(promocao).enviar();
    }

    @Test
    void falhaNaPersistenciaNaoGeraRelatorioNemPromocao() {
        when(repository.save(any(Livro.class))).thenThrow(new IllegalStateException("Banco indisponível"));
        assertThrows(IllegalStateException.class, () -> service.cadastrar(livro("HQ", 100.0)));
        verifyNoInteractions(relatorio, promocao);
    }

    @Test
    void atualizaSemReaplicarDescontoEPreservaId() {
        service = new LivroService(repository,
                List.of(new DescontoPorCategoria("TECNOLOGIA", 0.8)), relatorio, promocao);
        Livro existente = new Livro(1L, "Antigo", "Autor antigo", 50.0, "ROMANCE");
        Livro entrada = new Livro(999L, "Novo", "Autor novo", 100.0, "TECNOLOGIA");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Livro resultado = service.atualizar(1L, entrada);
        assertEquals(1L, resultado.getId());
        assertEquals("Novo", resultado.getTitulo());
        assertEquals("Autor novo", resultado.getAutor());
        assertEquals("TECNOLOGIA", resultado.getCategoria());
        assertEquals(100.0, resultado.getPreco(), 0.00001);
        verifyNoInteractions(relatorio, promocao);
    }

    @Test
    void atualizacaoInvalidaNaoAlteraLivroExistente() {
        Livro existente = new Livro(1L, "Antigo", "Autor", 50.0, "ROMANCE");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(RegraNegocioException.class, () -> service.atualizar(1L, livro("HQ", null)));
        assertEquals("Antigo", existente.getTitulo());
        assertEquals(50.0, existente.getPreco(), 0.00001);
        verify(repository, never()).save(any());
    }

    @Test
    void protegeLivroRaro() {
        Livro raro = livro("RARO", 100.0);
        when(repository.findById(1L)).thenReturn(Optional.of(raro));
        assertThrows(RegraNegocioException.class, () -> service.remover(1L));
        verify(repository, never()).delete(any(Livro.class));
    }

    @Test
    void removeLivroComum() {
        Livro comum = livro("HQ", 100.0);
        when(repository.findById(1L)).thenReturn(Optional.of(comum));
        service.remover(1L);
        verify(repository).delete(comum);
    }

    @Test
    void informaLivroInexistenteNaAtualizacaoERemocao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LivroNaoEncontradoException.class, () -> service.atualizar(99L, livro("HQ", 100.0)));
        assertThrows(LivroNaoEncontradoException.class, () -> service.remover(99L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(Livro.class));
    }

    private Livro livro(String categoria, Double preco) {
        return new Livro(null, "Livro", "Autor", preco, categoria);
    }
}
