package com.poo.atividade_refac.services;

import com.poo.atividade_refac.contracts.DescontoLivro;
import com.poo.atividade_refac.contracts.LivroOperations;
import com.poo.atividade_refac.contracts.PromocaoLivros;
import com.poo.atividade_refac.contracts.RelatorioLivros;
import com.poo.atividade_refac.entities.Livro;
import com.poo.atividade_refac.exceptions.LivroNaoEncontradoException;
import com.poo.atividade_refac.exceptions.RegraNegocioException;
import com.poo.atividade_refac.repositories.LivroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService implements LivroOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(LivroService.class);
    private final LivroRepository repository;
    private final List<DescontoLivro> descontos;
    private final RelatorioLivros relatorio;
    private final PromocaoLivros promocao;

    public LivroService(LivroRepository repository, List<DescontoLivro> descontos,
                        RelatorioLivros relatorio, PromocaoLivros promocao) {
        this.repository = repository;
        this.descontos = List.copyOf(descontos);
        this.relatorio = relatorio;
        this.promocao = promocao;
    }

    @Override
    public List<Livro> listar() {
        LOGGER.info("Listando livros");
        return repository.findAll();
    }

    @Override
    public Livro cadastrar(Livro livro) {
        validar(livro);
        descontos.stream()
                .filter(desconto -> desconto.aceita(livro.getCategoria()))
                .findFirst()
                .ifPresent(desconto -> livro.setPreco(desconto.aplicar(livro.getPreco())));
        LOGGER.info("Salvando livro: {}", livro.getTitulo());
        Livro salvo = repository.save(livro);
        relatorio.gerar();
        promocao.enviar();
        return salvo;
    }

    @Override
    public Livro atualizar(Long id, Livro livro) {
        Livro existente = buscar(id);
        validar(livro);
        existente.setTitulo(livro.getTitulo());
        existente.setAutor(livro.getAutor());
        existente.setPreco(livro.getPreco());
        existente.setCategoria(livro.getCategoria());
        return repository.save(existente);
    }

    @Override
    public void remover(Long id) {
        Livro livro = buscar(id);
        if ("RARO".equals(livro.getCategoria())) {
            throw new RegraNegocioException("Livros raros não podem ser removidos");
        }
        repository.delete(livro);
    }

    private Livro buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(id));
    }

    private void validar(Livro livro) {
        if (livro == null || livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new RegraNegocioException("Título obrigatório");
        }
        Double preco = livro.getPreco();
        if (preco == null || !Double.isFinite(preco) || preco <= 0) {
            throw new RegraNegocioException("Preço inválido");
        }
    }
}
