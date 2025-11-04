FROM clojure:temurin-21-lein-alpine AS build

WORKDIR /app

# Copiar arquivos de configuração do projeto
COPY project.clj /app/

# Baixar dependências
RUN lein deps

# Copiar código fonte
COPY src /app/src

# Criar uberjar
RUN lein uberjar

# Imagem final
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar uberjar da etapa de build
COPY --from=build /app/target/uberjar/api-gateway-*-standalone.jar /app/api-gateway.jar

# Instalar curl para healthcheck
RUN apk add --no-cache curl

# Expor porta
EXPOSE 8080

# Executar aplicação
CMD ["java", "-jar", "/app/api-gateway.jar"]
