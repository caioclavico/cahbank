Arquitetura do CahBank

## 🏗️ Visão Geral da Arquitetura

O CahBank foi projetado seguindo os princípios da **Arquitetura Hexagonal** (Ports & Adapters), garantindo alta testabilidade, manutenibilidade e flexibilidade.

## Princípios Arquiteturais

### **1. Arquitetura Hexagonal**

- **Core**: Lógica de negócio isolada
- **Ports**: Interfaces que definem contratos
- **Adapters**: Implementações específicas de infraestrutura

### **2. Event-Driven Architecture**

- **Comandos**: Ações que modificam o estado
- **Eventos**: Notificações de mudanças de estado
- **Assíncrono**: Processamento não-bloqueante

### **3. CQRS (Command Query Responsibility Segregation)**

- **Commands**: Modificam o estado do sistema
- **Queries**: Consultam o estado atual
- **Separação**: Responsabilidades distintas

## 🏢 Estrutura de Camadas

### **Domain Layer**

```
domain/
├── model/          # Entidades e Value Objects
├── event/          # Eventos de domínio
└── service/        # Serviços de domínio
```

### **Application Layer**

```
application/
├── port/
│   ├── in/         # Driving Ports (Interfaces de entrada)
│   └── out/        # Driven Ports (Interfaces de saída)
└── service/        # Serviços de aplicação
```

### **Infrastructure Layer**

```
infrastructure/
├── persistence/    # Repositórios e banco de dados
├── messaging/      # Kafka producers e consumers
└── web/            # APIs REST
```

# 🔌 Ports & Adapters

### **Driving Ports (Inbound)**

- **AccountService**: Interface para operações de conta
- **TransactionService**: Interface para operações de transação

### **Driven Ports (Outbound)**

- **AccountRepository**: Interface para persistência de contas
- **TransactionRepository**: Interface para persistência de transações
- **EventPublisher**: Interface para publicação de eventos
