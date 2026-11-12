Agendamento para Barbearia (versão de demonstração)
Sistema web de agendamento em que o cliente escolhe serviço, barbeiro e horário pelo celular e vê só horários realmente livres , respeitando expediente, horário, dias de atendimento e horários do barbeiro.

Este repositório é a versão de demonstração : página do cliente, regras de agenda, API REST e segurança. A versão completa (painel do dono, taxas por falta e relatório de comissão) fica em repositório privado.

Status: projeto de estudo. Ainda não foi testado de forma automatizada e não foi usado na produção.

Telas
A página do cliente ( /) tem quatro passos: serviço, barbeiro, dia e horário, e dados do cliente. No fim, ela mostra um comprovante com código.

O que a demonstração mostra
Horários livres calculando no servidor: a consulta de disponibilidade desconta expediente, intervalo de almoço, dias em que o barbeiro não atende, horários, agendamentos existentes e horários que já passaram.
Sem agendamentos sobrepostos: uma consulta de conflito, combinada com bloqueio pessimista no banco, garante que dois pedidos ao mesmo tempo não reservem o mesmo horário.
Cliente sem cadastro aviso: o agendamento público local o cliente pelo celular ou cria um novo.
API REST em camadas (controlador, serviço, repositório) com validação e erros padronizados.
Rotas protegidas por login: agenda do dia, clientes, cadastros e folgas desativar autenticação.
Dados fictícios prontos: 2 barbeiros, 5 serviços e uma folga de exemplo são criados na primeira execução.
Decisões técnicas
Conflito de horários: a consulta verifica novoInício < fimExistente && novoFim > inícioExistentepara o mesmo barbeiro, considerando apenas agendamentos ativos e folgas.
Concorrência: o agendamento e a remarcação travam a linha do barbeiro ( PESSIMISTIC_WRITE) dentro da transação, então a verificação de conflito e o recebimento insertsem que outro pedido entre no meio.
Preço congelado: o valor do serviço é gravado no agendamento, e altera a tabela de preços depois não altera o histórico.
Autenticação: sessão Spring Security por cookie e proteção CSRF (token em cookie lido pelo JavaScript). Só que a página do cliente precisa ser pública, e o endpoint público de barbeiros não expõe dados internos.
A senha fora do código: vem de variável de ambiente ou do arquivo .env, e é guardada com BCrypt na memória.
Tecnologias
Java 17, Spring Boot 3, Spring Data JPA, Spring Security, Bean Validation
PostgreSQL 16 (via Docker)
Frontend em HTML, CSS e JavaScript puro, servido pelo próprio Spring Boot
Como rodar
Pré-requisitos
Java 17 ou superior
Maven 3.9+ , ou a extensão "Extension Pack for Java" no VS Code
Docker Desktop para banco de dados
Passo a passo
Baixe o projeto e entre na pasta que contém o pom.xml:

git clone https://github.com/Ygor010203/agendamento.git
cd agendamento
Crie o arquivo .enva partir do exemplo e defina a senha:

# Windows (PowerShell)
copy .env.exemplo .env
# Linux ou Mac
cp .env.exemplo .env
Abra o .enve responda ADMIN_PASSWORDcom pelo menos 8 caracteres . Sem isso a aplicação não inicia.

Suba o banco de dados:

docker compose up -d
Inicie uma aplicação:

mvn spring-boot:run
No VS Code, também dá para abrir AgendamentoApplication.javae clicar em Run acima do método main.

Abra http://localhost:8080 e faça um agendamento.

Login e rotas protegidas
Abra http://localhost:8080/login.html e entre com admina senha do .env.
Com sessão ativa, abra no navegador, por exemplo:
/api/barbeiros/1/agenda?data=AAAA-MM-DDpara ver os agendamentos do dia
/api/clientese/api/barbeiros
Sem login,essas rotas respondem 401 . Alterações ( POST, PUT, PATCH, DELETE) também desativam o token CSRF no cabeçalho X-XSRF-TOKEN, lido do cookie XSRF-TOKEN.
Configuração
Variável	Padrão	Descrição
ADMIN_USER	admin	Usuário do dono
ADMIN_PASSWORD	(obrigatória)	Palavra do dono, mínimo de 8 caracteres
DB_URL	jdbc:postgresql://localhost:5432/agendamento	Endereço do banco
DB_USER	postgres	Usuário do banco
DB_PASSWORD	postgres	Senha do banco
COOKIE_SECURE	false	Use trueem produção, com HTTPS
API
Rotas abertas (usadas pela página do cliente):

Método	Rota	Descrição
OBTER	/api/publico/servicos	Lista de serviços
OBTER	/api/publico/barbeiros	Lista de barbeiros
OBTER	/api/barbeiros/{id}/disponibilidade?servicoId=1&data=2026-11-10	Horários livres
POSTAGEM	/api/agendamentos/publico	Agendamento feito pelo cliente
Rotas que login:

Método	Rota	Descrição
POST, GET, PUT, DELETE	/api/clientes, /api/barbeiros,/api/servicos	Cadastros
OBTER	/api/barbeiros/{id}/agenda?data=2026-11-10	Agenda do dia
POST, GET	/api/barbeiros/{id}/bloqueios	Folhas do barbeiro
EXCLUIR	/api/bloqueios/{id}	Remover uma lacuna
OBTER	/api/agendamentos/{id}	Consulta de um agendamento
PATCH	/api/agendamentos/{id}/remarcar	Remarcação
PATCH	/api/agendamentos/{id}/cancelar	Cancelar
OBTER	/api/clientes/{id}/agendamentos	Histórico do cliente
Estrutura do projeto
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
Limitações conhecidas
Há um único usuário (o dono), sem contas por barbeiro
Não há limite de esforço de login
A página pública aceita qualquer nome e celular, sem confirmação
O esquema do banco está atualizado por ddl-auto=update, sem migrações versionadas
Sem testes automatizados
Próximos passos
Testes automatizados das regras de agenda
Migrações com Flyway
Limite de tentativa de login e confirmação do celular
Cancelamento e remarcação feitos pelo próprio cliente
Autor
Feito por Ygor .

Licença
Este projeto usa a licença PolyForm Noncommercial 1.0.0 : o código pode ser lido, treinado e usado para fins não comerciais. O uso comercial exige autorização do autor. Veja o arquivo LICENSE.
