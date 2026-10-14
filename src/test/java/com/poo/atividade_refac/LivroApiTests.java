package com.poo.atividade_refac;

import com.poo.atividade_refac.entities.Livro;
import com.poo.atividade_refac.repositories.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LivroApiTests {
    @LocalServerPort
    private int port;

    @Autowired
    private LivroRepository repository;

    private RestClient client;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        client = RestClient.create("http://localhost:" + port);
    }

    @ParameterizedTest
    @CsvSource({"ROMANCE,90", "TECNOLOGIA,80", "INFANTIL,70", "HQ,60", "OUTRA,100", "RARO,100"})
    void cadastroAplicaDescontoEPersiste(String categoria, double esperado) {
        Livro salvo = cadastrar(categoria);
        assertNotNull(salvo.getId());
        assertEquals(esperado, salvo.getPreco(), 0.00001);
        assertEquals(esperado, repository.findById(salvo.getId()).orElseThrow().getPreco(), 0.00001);
    }

    @Test
    void categoriaNulaMantemPrecoOriginal() {
        assertEquals(100.0, cadastrar(null).getPreco(), 0.00001);
    }

    @Test
    void percorreCadastroListagemAtualizacaoERemocao() {
        Livro salvo = cadastrar("TECNOLOGIA");
        Livro[] listagem = client.get().uri("/livros").retrieve().body(Livro[].class);
        assertNotNull(listagem);
        assertEquals(1, listagem.length);
        assertEquals(salvo.getId(), listagem[0].getId());

        Livro entrada = new Livro(null, "Atualizado", "Outro autor", 150.0, "TECNOLOGIA");
        Livro atualizado = client.put().uri("/livros/{id}", salvo.getId())
                .body(entrada).retrieve().body(Livro.class);
        assertNotNull(atualizado);
        assertEquals("Atualizado", atualizado.getTitulo());
        assertEquals(150.0, atualizado.getPreco(), 0.00001);
        assertEquals(salvo.getId(), atualizado.getId());

        client.delete().uri("/livros/{id}", salvo.getId()).retrieve().toBodilessEntity();
        assertFalse(repository.existsById(salvo.getId()));
    }

    @Test
    void retorna400ParaPrecoNuloETituloEmBranco() {
        for (Livro invalido : new Livro[]{
                new Livro(null, "Livro", "Autor", null, "HQ"),
                new Livro(null, " ", "Autor", 100.0, "HQ")}) {
            var erro = assertThrows(HttpClientErrorException.class,
                    () -> client.post().uri("/livros").body(invalido).retrieve().toBodilessEntity());
            assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        }
        assertEquals(0, repository.count());
    }

    @Test
    void atualizacaoInvalidaRetorna400EPreservaDados() {
        Livro salvo = cadastrar("HQ");
        var erro = assertThrows(HttpClientErrorException.class,
                () -> client.put().uri("/livros/{id}", salvo.getId())
                        .body(new Livro(null, "Alterado", "Autor", -1.0, "TECNOLOGIA"))
                        .retrieve().toBodilessEntity());
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        Livro existente = repository.findById(salvo.getId()).orElseThrow();
        assertEquals("Livro", existente.getTitulo());
        assertEquals(60.0, existente.getPreco(), 0.00001);
        assertEquals("HQ", existente.getCategoria());
    }

    @Test
    void retorna400ENaoRemoveLivroRaro() {
        Livro raro = cadastrar("RARO");
        var erro = assertThrows(HttpClientErrorException.class,
                () -> client.delete().uri("/livros/{id}", raro.getId()).retrieve().toBodilessEntity());
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        assertTrue(erro.getResponseBodyAsString().contains("Livros raros não podem ser removidos"));
        assertTrue(repository.existsById(raro.getId()));
    }

    @Test
    void retorna404ParaLivroInexistente() {
        var remocao = assertThrows(HttpClientErrorException.class,
                () -> client.delete().uri("/livros/999999").retrieve().toBodilessEntity());
        assertEquals(HttpStatus.NOT_FOUND, remocao.getStatusCode());
        var atualizacao = assertThrows(HttpClientErrorException.class,
                () -> client.put().uri("/livros/999999")
                        .body(new Livro(null, "Livro", "Autor", 100.0, "HQ"))
                        .retrieve().toBodilessEntity());
        assertEquals(HttpStatus.NOT_FOUND, atualizacao.getStatusCode());
    }

    private Livro cadastrar(String categoria) {
        Livro salvo = client.post().uri("/livros")
                .body(new Livro(null, "Livro", "Autor", 100.0, categoria))
                .retrieve().body(Livro.class);
        assertNotNull(salvo);
        return salvo;
    }
}
