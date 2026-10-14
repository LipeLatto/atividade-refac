# Refatoração da API de livros

O principal problema do código original era a concentração de responsabilidades no `LivroController`. Além de receber as requisições, ele validava dados, calculava descontos, acessava o banco, gerava relatório e simulava o envio de email. A interface `LivroOperations` também exigia métodos que não tinham implementação.

## Princípios SOLID aplicados

- **SRP — responsabilidade única:** o controller passou a tratar as requisições HTTP. As operações e regras dos livros ficaram no `LivroService`; relatório e promoção receberam implementações separadas.
- **OCP — aberto para extensão:** os descontos deixaram de depender de uma sequência de `if/else`. O serviço recebe estratégias `DescontoLivro`, e novas regras podem ser registradas sem alterar o cadastro.
- **LSP — substituição:** os métodos que apenas lançavam `UnsupportedOperationException` foram retirados do contrato. As implementações agora oferecem as operações que seus contratos prometem.
- **ISP — segregação de interfaces:** `LivroOperations` contém somente as operações CRUD. Relatório e promoção usam contratos próprios, evitando obrigar uma classe a implementar funções que não utiliza.
- **DIP — inversão de dependência:** o controller depende de `LivroOperations`. O serviço recebe os contratos de desconto, relatório, promoção e persistência por construtor, permitindo substituir suas implementações e testar as regras isoladamente.

## Funcionamento e correções

Os endpoints `GET /livros`, `POST /livros`, `PUT /livros/{id}` e `DELETE /livros/{id}` foram mantidos, assim como os campos da entidade e o banco H2 em memória.

No cadastro, os descontos continuam sendo 10% para ROMANCE, 20% para TECNOLOGIA, 30% para INFANTIL e 40% para HQ. Categorias sem estratégia mantêm o preço original. A atualização não reaplica desconto, e livros da categoria RARO continuam protegidos contra remoção.

Título obrigatório e preço positivo passaram a ser validados também na atualização. Preços nulos e não finitos são rejeitados. Dados inválidos e tentativa de excluir um livro raro retornam HTTP 400; operações sobre livros inexistentes retornam HTTP 404.

Os logs usam SLF4J. O relatório conta os registros com `count()`, sem carregar todos os livros. O email continua sendo uma simulação por log.

## Verificação

A compilação e os 30 testes passaram com JDK 22: 17 testes das regras do serviço, 12 testes HTTP com H2 e 1 teste de inicialização. A suíte verifica os descontos, o fluxo CRUD, as validações, a proteção de livros raros e as respostas 400/404.

Para executar: `gradlew.bat bootRun`. Para testar: `gradlew.bat test`.

O projeto original usava JDK 21. Esta versão está configurada para JDK 22. O agente do Mockito é carregado na inicialização dos testes para evitar problemas de anexação dinâmica no Java.
