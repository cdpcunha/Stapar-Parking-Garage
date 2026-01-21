Estapar Garage Management System - Tech Lead Challenge
Este projeto consiste em um sistema de gerenciamento de estacionamento que consome eventos de um simulador, gerencia a ocupação de vagas, aplica regras de preço dinâmico e calcula receita por setor.

🛠 Decisões de Arquitetura e Tecnologias
1. Stack Técnica
   Java 21: Uso de Records para DTOs (imutabilidade e concisão), Virtual Threads (implícito na performance do Spring Boot 3.2+) e novas APIs de data/hora.

Spring Boot 4.0.x: Base robusta para APIs REST e integração com Banco de Dados.

MySQL 8.0: Escolhido como banco de persistência principal conforme requisitos.

H2 Database: Utilizado para Testes de Integração e desenvolvimento rápido via profile test.

Flyway / Hibernate DDL: Estratégia de evolução de esquema (DDL auto-update para o teste, mas preparado para migrações).

2. Padrões de Design e Lógica de Negócio
   Preço Dinâmico (Fator de Demanda): O cálculo do fator de preço (0.9x a 1.25x) ocorre no momento do evento ENTRY. Isso garante que o preço "congelado" na entrada seja respeitado no fechamento, independentemente da variação da lotação durante a estadia.

RFC 7807 (Problem Details): Implementação de um GlobalExceptionHandler padronizado para tratamento de erros, retornando JSONs informativos e códigos HTTP semânticos.

Estratégia de Sincronização: Uso de um ApplicationRunner que consome o endpoint /garage do simulador no startup para garantir que o banco local tenha as configurações físicas de setores e coordenadas.

🚀 Como Rodar o Projeto
Pré-requisitos
Docker e Docker Compose

Java 21 instalado (JDK)

IDE de preferência (IntelliJ recomendada)

Passos para execução
Subir Infraestrutura: No terminal, na raiz do projeto, execute:

Bash

docker-compose up -d
Isso subirá o MySQL (porta 3306) e o simulador da Estapar.

Configurar Aplicação: Certifique-se de que o application.properties aponta para o MySQL do Docker:

Properties

spring.datasource.url=jdbc:mysql://localhost:3306/estapar_db
spring.datasource.username=root
spring.datasource.password=root
server.port=3003
Executar:

Bash

./gradlew bootRun
🧪 Testes
A suíte de testes foi desenhada para cobrir desde a lógica matemática até a integração de endpoints.

Executar todos os testes
Bash

./gradlew test
Estratégia de Teste
Unitários (ParkingServiceTest): Focam na exatidão do cálculo de horas (arredondamento), carência de 30 minutos e aplicação dos fatores de preço dinâmico.

Integração (FullFlowIntegrationTest): Utiliza H2 em memória e MockMvc para simular o ciclo completo de um carro (ENTRY -> PARKED -> EXIT) sem necessidade de rede real, validando a persistência e a resposta final de receita.

📖 Guia da API
Webhook (Consumido pelo Simulador)
Endpoint: POST /webhook

Eventos: ENTRY, PARKED, EXIT.

Consulta de Receita
Endpoint: GET /revenue

Request Body:

JSON

{
"date": "2025-01-01",
"sector": "A"
}
Response:

JSON

{
"amount": 27.00,
"currency": "BRL",
"timestamp": "2025-01-21T10:00:00Z"
}
⚠️ Tratamento de Erros
O sistema utiliza um manipulador global para converter exceções em respostas amigáveis:

409 Conflict: Tentativa de entrada com garagem lotada ou placa já registrada.

404 Not Found: Referência a setor ou vaga inexistente.

400 Bad Request: Dados de entrada inválidos ou malformados.

Considerações Finais do Desenvolvedor
Este projeto foi estruturado pensando em escalabilidade. A separação clara entre a lógica de geolocalização (vagas) e a lógica financeira (sessões) permite que novos modelos de cobrança sejam adicionados com mínimo impacto no código existente.