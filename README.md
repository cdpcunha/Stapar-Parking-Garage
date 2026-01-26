Estapar Garage Management System - Tech Lead Challenge
Este projeto consiste num sistema de gestão de estacionamento que consome eventos de um simulador, gerencia a ocupação de vagas, aplica regras de preço dinâmico e calcula receita por setor.

Decisões de Arquitetura e Tecnologias
1. Stack Técnica
   Java 21: Uso de Records para DTOs (imutabilidade e concisão), evitando alteração do estado de um objeto que está a ser usado ainda por outro método.
      Versão do java foi mencionada no desafio obrigatoriamente, mas como é a versão cujo avaliado possui mais experiência, não há necessidade de mudança.

Spring Boot 4.0.1: Base robusta para APIs REST e integração com Banco de Dados. Moderna e padrão de mercado, não é a mais atualizada, mas mesmo assim em acordo com grandes empresas.

MySQL 8.0: Escolhido como banco de persistência principal conforme requisitos da própria atividade.

Inicialmente havia sido levantada a possibilidade de testes em memória via H2, mas não houve necessidade.

2. Padrões de ‘Design’ e Lógica de Negócio
   Preço Dinâmico (Fator de Demanda): O cálculo do fator de preço (0.9x a 1.25x) ocorre no momento do evento ENTRY. Isso garante que o preço "congelado" na entrada seja respeitado no fechamento, independentemente da variação da lotação durante a estadia.
   
   Implementação de um GlobalExceptionHandler padronizado para tratamento de erros, retornando JSONs informativos e códigos HTTP semânticos.

Estratégia de Sincronização: Uso de um ApplicationRunner que consome o endpoint /garage do simulador no startup para garantir que o banco local tenha as configurações físicas de setores e coordenadas.

3. Pré-requisitos 
Docker e Docker Compose
Java 21 instalado (JDK)
IDE de preferência ou execução padrão via arquivo binário.

4. Passos para execução
Subir Infraestrutura: No terminal, na raiz do projeto, execute:

docker-compose up -d

Isso subirá o MySQL (porta 3306) e o simulador da Estapar na porta 3000.

5. Configurar Aplicação: Certifique-se de que o application.properties aponta para o MySQL do Docker:

Executar Aplicação: ./gradlew bootRun ou execução pela IDE.

6. Testes
Os testes foram desenhados para cobrir desde a lógica matemática até a integração de endpoints.

Executar todos os testes

./gradlew test

Estratégia de Teste
Unitários (ParkingServiceTest): Focam na exatidão do cálculo de horas (arredondamento), carência de 30 minutos e aplicação dos fatores de preço dinâmico.

Integração (FullFlowIntegrationTest): MockMvc para simular o ciclo completo de um carro (ENTRY -> PARKED -> EXIT).