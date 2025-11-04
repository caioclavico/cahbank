# 🏦 CahBank - Digital Bank Backend

Aplicação de banco digital construída com Clojure, seguindo Arquitetura Hexagonal e Event Sourcing.

## 🚀 Quick Start

### Com Docker (Produção)

```bash
docker-compose up -d      # Iniciar
docker-compose logs -f    # Ver logs
docker-compose down       # Parar
```

### Com Leiningen (Desenvolvimento)

```bash
# Terminal 1: Backend (portas 8081, 8082)
cd backend && lein run

# Terminal 2: API Gateway (porta 8080)
cd api-gateway && lein run

# Parar: Ctrl+C em cada terminal
```

#### Rodar serviços individuais

```bash
# Apenas Account Service (porta 8081)
cd backend && lein run account

# Apenas Transaction Service (porta 8082)
cd backend && lein run transaction
```

---

## 📡 APIs

### Account Service (8081)

```bash
# Criar conta
curl -X POST http://localhost:8081/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "João Silva", "email": "joao@email.com", "document": "12345678900"}'

# Buscar conta
curl http://localhost:8081/accounts/{id}

# Listar contas
curl http://localhost:8081/accounts
```

### Transaction Service (8082)

```bash
# Depósito
curl -X POST http://localhost:8082/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{"account_id": "uuid", "amount": 100.0}'

# Saque
curl -X POST http://localhost:8082/transactions/withdraw \
  -H "Content-Type: application/json" \
  -d '{"account_id": "uuid", "amount": 50.0}'

# Transferência
curl -X POST http://localhost:8082/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{"from_account_id": "uuid1", "to_account_id": "uuid2", "amount": 75.0}'

# Listar transações
curl http://localhost:8082/transactions/account/{account_id}
```

### API Gateway (8080)

```bash
# Via Gateway
curl http://localhost:8080/accounts
curl http://localhost:8080/transactions/account/{id}
```

---

## 🏗️ Arquitetura

### Stack

- **Linguagem**: Clojure 1.12.2
- **Web**: Ring + Compojure + Jetty
- **Message Broker**: Apache Kafka
- **Database**: Apache Cassandra
- **Build**: Leiningen
- **Container**: Docker + Docker Compose

### Componentes

```
┌─────────────────┐
│  API Gateway    │  :8080
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌───────────┐
│Account │ │Transaction│  :8081, :8082
└───┬────┘ └────┬──────┘
    │           │
    └─────┬─────┘
          ▼
    ┌──────────┐
    │  Kafka   │
    └────┬─────┘
         │
    ┌────▼────┐
    │Cassandra│
    └─────────┘
```

### Estrutura

```
cahbank/
├── backend/
│   ├── src/backend/
│   │   ├── account/              # Account Service
│   │   │   ├── application/      # Use cases
│   │   │   ├── domain/           # Entities & Events
│   │   │   └── infrastructure/   # DB, Kafka, HTTP
│   │   ├── transaction/          # Transaction Service
│   │   └── shared/               # Cassandra, Kafka config
│   └── project.clj
├── api-gateway/
│   ├── src/api_gateway/
│   │   ├── gateway/              # Proxy & Router
│   │   └── middleware/           # Auth, Rate Limit, CORS
│   └── project.clj
└── docker-compose.yml
```

---

## 🔧 Desenvolvimento

### Pré-requisitos

- Java 11+
- Leiningen 2.9+
- Docker (opcional)

### Instalar Leiningen

```bash
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o /usr/local/bin/lein
chmod +x /usr/local/bin/lein
lein version
```

### Build

```bash
# Backend
cd backend && lein uberjar

# API Gateway
cd api-gateway && lein uberjar
```

### Testes

```bash
cd backend && lein test
cd api-gateway && lein test
```

---

## 🐳 Docker

```bash
# Build
docker-compose build

# Subir
docker-compose up -d

# Status
docker-compose ps

# Logs
docker-compose logs -f backend
docker-compose logs -f api-gateway

# Parar
docker-compose down

# Limpar volumes
docker-compose down -v
```

---

## 🛠️ Troubleshooting

### Ver portas em uso

```bash
lsof -i :8080,8081,8082 | grep LISTEN
```

### Matar processos

```bash
pkill -9 -f "lein.*(backend|api-gateway)"
```

### Limpar Docker

```bash
docker-compose down -v
docker system prune -a
```

---

## �� Fluxo de Dados

### Criação de Conta

```
POST /accounts
→ Gateway (8080)
→ Account Service (8081)
→ Kafka (account-cmds)
→ Account Consumer
→ Cassandra
→ Kafka (account-events)
← 202 Accepted
```

### Transação

```
POST /transactions/deposit
→ Gateway (8080)
→ Transaction Service (8082)
→ Kafka (transaction-cmds)
→ Transaction Consumer
→ Cassandra (transaction)
→ Kafka (transaction-events)
→ Balance Updater Consumer
→ Cassandra (atualiza saldo)
← 202 Accepted
```

---

## 🎯 Funcionalidades

- ✅ Criação de contas
- ✅ Depósitos, saques e transferências
- ✅ Consulta de saldo e histórico
- ✅ Event Sourcing (Kafka)
- ✅ Atualização automática de saldos
- ✅ API Gateway com rate limiting e CORS
- ✅ Graceful shutdown (Ctrl+C)
- ✅ Docker support
- ✅ Controle individual de serviços

---

## 📝 Princípios Arquiteturais

- **Hexagonal Architecture**: Separação domínio/aplicação/infraestrutura
- **Event Sourcing**: Eventos como fonte da verdade
- **CQRS**: Comandos (write) vs Consultas (read)
- **Event-Driven**: Comunicação assíncrona

---

**Desenvolvido com ❤️ usando Clojure**
