# CahBank - Digital Bank Backend

## 📋 Visão Geral

CahBank é uma aplicação de banco digital construída com Clojure, seguindo os princípios da Arquitetura Hexagonal (Ports & Adapters). A aplicação é composta por microserviços que gerenciam contas bancárias e transações financeiras.

## 🏗️ Arquitetura

### **Princípios Arquiteturais**

- **Arquitetura Hexagonal**: Separação clara entre domínio, aplicação e infraestrutura
- **Event-Driven**: Comunicação assíncrona via Kafka
- **CQRS**: Separação entre comandos (escrita) e consultas (leitura)
- **Microserviços**: Serviços independentes e desacoplados

### **Tecnologias**

- **Linguagem**: Clojure 1.12.2
- **Message Broker**: Apache Kafka
- **Database**: Apache Cassandra
- **Web Framework**: Ring + Compojure
- **Build Tool**: Leiningen

## 🚀 Serviços

### **Account Service (Porta 8081)**

Gerencia o ciclo de vida das contas bancárias:

- Criação de contas
- Atualização de dados
- Fechamento de contas
- Consulta de contas

### **Transaction Service (Porta 8082)**

Gerencia transações financeiras:

- Depósitos
- Saques
- Transferências
- Consulta de histórico

## 📁 Estrutura do Projeto

```
backend/
├── src/backend/
│   ├── account/                    # Account Service
│   │   ├── application/            # Camada de aplicação
│   │   ├── domain/                 # Camada de domínio
│   │   └── infrastructure/         # Camada de infraestrutura
│   ├── transaction/                # Transaction Service
│   │   ├── application/            # Camada de aplicação
│   │   ├── domain/                 # Camada de domínio
│   │   └── infrastructure/         # Camada de infraestrutura
│   └── shared/                     # Componentes compartilhados
├── docker-compose.yml              # Orquestração de serviços
└── project.clj                     # Configuração do projeto
```
