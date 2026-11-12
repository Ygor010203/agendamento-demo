# Agendamento para Barbearia (versão de demonstração)

Sistema web de agendamento em que o cliente escolhe serviço, barbeiro e horário pelo celular e vê **só horários realmente livres**, respeitando expediente, almoço, dias de atendimento e folgas do barbeiro.

Este repositório é a **versão de demonstração**: página do cliente, regras de agenda, API REST e segurança. A versão completa (painel do dono, taxas por falta e relatório de comissão) fica em repositório privado.

> **Status:** projeto de estudo. Ainda não tem testes automatizados e não foi usado em produção.

## Telas

<!--
Adicione prints ou um GIF curto na pasta docs/ e descomente as linhas abaixo:

![Escolha de serviço](docs/servico.png)
![Escolha de horário](docs/horario.png)
-->

A página do cliente (`/`) tem quatro passos: serviço, barbeiro, dia e horário, e dados do cliente. No fim, ela mostra um comprovante com código.

## O que a demonstração mostra

- **Horários livres calculados no servidor:** a consulta de disponibilidade desconta expediente, intervalo de almoço, dias em que o barbeiro não atende, folgas, agendamentos existentes e horários que já passaram.
- **Sem agendamentos sobrepostos:** uma consulta de conflito, combinada com bloqueio pessimista no banco, garante que dois pedidos ao mesmo tempo não reservem o mesmo horário.
- **Cliente sem cadastro prévio:** o agendamento público localiza o cliente pelo celular ou cria um novo.
- **API REST em camadas** (controller, service, repository) com validação e erros padronizados.
- **Rotas protegidas por login:** agenda do dia, clientes, cadastros e folgas exigem autenticação.
- **Dados fictícios prontos:** 2 barbeiros, 5 serviços e uma folga de exemplo são criados na primeira execução.

## Decisões técnicas

- **Conflito de horários:** a consulta verifica `novoInício < fimExistente && novoFim > inícioExistente` para o mesmo barbeiro, considerando só agendamentos ativos e folgas.
- **Concorrência:** o agendamento e a remarcação travam a linha do barbeiro (`PESSIMISTIC_WRITE`) dentro da transação, então a checagem de conflito e o `insert` acontecem sem que outro pedido entre no meio.
- **Preço congelado:** o valor do serviço é gravado no agendamento, e mudar a tabela de preços depois não altera o histórico.
- **Autenticação:** Spring Security com sessão por cookie e proteção CSRF (token em cookie lido pelo JavaScript). Só o que a página do cliente precisa é público, e o endpoint público de barbeiros não expõe dados internos.
- **Senha fora do código:** vem de variável de ambiente ou do arquivo `.env`, e é guardada com BCrypt em memória.

## Tecnologias

- Java 17, Spring Boot 3, Spring Data JPA, Spring Security, Bean Validation
- PostgreSQL 16 (via Docker)
- Frontend em HTML, CSS e JavaScript puros, servido pelo próprio Spring Boot

## Como rodar

### Pré-requisitos
- [Java 17 ou superior](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi), ou a extensão "Extension Pack for Java" no VS Code
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) para o banco de dados

### Passo a passo

1. Baixe o projeto e entre na pasta que contém o `pom.xml`:
   ```bash
   git clone https://github.com/Ygor010203/agendamento.git
   cd agendamento
   ```

2. Crie o arquivo `.env` a partir do exemplo e defina a senha:
   ```bash
   # Windows (PowerShell)
   copy .env.exemplo .env
   # Linux ou Mac
   cp .env.exemplo .env
   ```
   Abra o `.env` e preencha `ADMIN_PASSWORD` com **pelo menos 8 caracteres**. Sem isso a aplicação não inicia.

3. Suba o banco de dados:
   ```bash
   docker compose up -d
   ```

4. Inicie a aplicação:
   ```bash
   mvn spring-boot:run
   ```
   No VS Code, também dá para abrir `AgendamentoApplication.java` e clicar em **Run** acima do método `main`.

5. Abra http://localhost:8080 e faça um agendamento.

### Login e rotas protegidas
1. Abra http://localhost:8080/login.html e entre com `admin` e a senha do `.env`.
2. Com a sessão ativa, abra no navegador, por exemplo:
   - `/api/barbeiros/1/agenda?data=AAAA-MM-DD` para ver os agendamentos do dia
   - `/api/clientes` e `/api/barbeiros`
3. Sem login, essas rotas respondem **401**. Alterações (`POST`, `PUT`, `PATCH`, `DELETE`) também exigem o token CSRF no cabeçalho `X-XSRF-TOKEN`, lido do cookie `XSRF-TOKEN`.

## Configuração

| Variável | Padrão | Descrição |
|---|---|---|
| `ADMIN_USER` | `admin` | Usuário do dono |
| `ADMIN_PASSWORD` | (obrigatória) | Senha do dono, mínimo de 8 caracteres |
| `DB_URL` | `jdbc:postgresql://localhost:5432/agendamento` | Endereço do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `COOKIE_SECURE` | `false` | Use `true` em produção, com HTTPS |

## API

Rotas abertas (usadas pela página do cliente):

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/publico/servicos` | Lista de serviços |
| GET | `/api/publico/barbeiros` | Lista de barbeiros |
| GET | `/api/barbeiros/{id}/disponibilidade?servicoId=1&data=2026-11-10` | Horários livres |
| POST | `/api/agendamentos/publico` | Agendamento feito pelo cliente |

Rotas que exigem login:

| Método | Rota | Descrição |
|---|---|---|
| POST, GET, PUT, DELETE | `/api/clientes`, `/api/barbeiros`, `/api/servicos` | Cadastros |
| GET | `/api/barbeiros/{id}/agenda?data=2026-11-10` | Agenda do dia |
| POST, GET | `/api/barbeiros/{id}/bloqueios` | Folgas do barbeiro |
| DELETE | `/api/bloqueios/{id}` | Remove uma folga |
| GET | `/api/agendamentos/{id}` | Consulta de um agendamento |
| PATCH | `/api/agendamentos/{id}/remarcar` | Remarca |
| PATCH | `/api/agendamentos/{id}/cancelar` | Cancela |
| GET | `/api/clientes/{id}/agendamentos` | Histórico do cliente |

## Estrutura do projeto

```
src/main/java/com/exemplo/agendamento
├── config/       segurança e login
├── controller/   endpoints REST
├── dto/          objetos de entrada e saída da API
├── exception/    erros de negócio e respostas padronizadas
├── model/        entidades (Cliente, Barbeiro, Servico, Agendamento, Bloqueio)
├── repository/   acesso ao banco (Spring Data JPA)
└── service/      regras de agendamento e de bloqueios

src/main/resources
├── application.properties
└── static/       página do cliente e tela de login
```

## Limitações conhecidas

- Há um único usuário (o dono), sem contas por barbeiro
- Não há limite de tentativas de login
- A página pública aceita qualquer nome e celular, sem confirmação
- O esquema do banco é atualizado por `ddl-auto=update`, sem migrações versionadas
- Sem testes automatizados

## Próximos passos

- Testes automatizados das regras de agenda
- Migrações com Flyway
- Limite de tentativas de login e confirmação do celular
- Cancelamento e remarcação feitos pelo próprio cliente

## Autor

Feito por [Ygor](https://github.com/Ygor010203).

## Licença

Este projeto usa a licença **PolyForm Noncommercial 1.0.0**: o código pode ser lido, estudado e usado para fins não comerciais. O uso comercial exige autorização do autor. Veja o arquivo `LICENSE`.
