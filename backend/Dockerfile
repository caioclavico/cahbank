FROM clojure:temurin-21-lein-2.11.2

WORKDIR /app

# Copiar arquivos de configuração primeiro (cache de dependências)
COPY project.clj /app/
RUN lein deps

# Copiar código fonte
COPY . /app/

# Compilar
RUN lein uberjar

# Porta do backend
EXPOSE 8081 8082

# Comando para iniciar
CMD ["java", "-jar", "target/uberjar/backend-0.1.0-SNAPSHOT-standalone.jar"]
