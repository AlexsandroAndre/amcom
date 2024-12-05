# **Order Management Service**

Este projeto é um serviço para gerenciamento de pedidos, implementado com Java Spring Boot, RabbitMQ e PostgreSQL. Ele processa pedidos recebidos de um sistema externo, 
calcula os valores dos produtos, e disponibiliza os dados processados para consulta.

## **Itens Desenvolvidos**

## 1. Recebimento de Pedidos

Integração com RabbitMQ para gerenciar a entrada de pedidos.
Configuração de exchange, fila e binding com o RabbitTemplate.
Listener para consumir e processar as mensagens recebidas.

## 2. Processamento de Pedidos

Cálculo do valor total dos produtos no pedido.
Geração de um identificador único para cada pedido (generateExternalId).
Persistência dos pedidos no banco de dados PostgreSQL.

## 3. Consulta de Pedidos

API REST para consulta de pedidos:
Paginação: Permite retornar grandes volumes de dados de forma eficiente.
Filtros: Pesquisa por status e intervalo de datas (início e fim).

## Pré-requisitos

Certifique-se de ter os seguintes itens instalados e configurados no ambiente:

Java 17
Maven
Docker (para o RabbitMQ)
PostgreSQL

## Como dar o Start no Projeto

### Passo 1: Subir o RabbitMQ

No diretório raiz do projeto, crie o arquivo docker-compose.yml (caso não exista):

```
yaml

version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

```

### 2. Execute o seguinte comando no terminal:

`docker-compose up -d`

### 3.Acesse o RabbitMQ Management em:

http://localhost:15672

Usuário: guest

Senha: guest

## Passo 2: Configurar o Banco de Dados

### 1. Crie um banco de dados no PostgreSQL com o nome order_management.
### 2. Configure o arquivo `application.yml` na pasta src/main/resources com as credenciais do banco:

```
yaml

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_management
    username: seu_usuario
    password: sua_senha
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

```

## Passo 3: Executar o Projeto
### 1.Compile e execute o projeto com os comandos abaixo:

```
mvn clean install
mvn spring-boot:run
```

### 2. A aplicação estará disponível em:
http://localhost:8080
