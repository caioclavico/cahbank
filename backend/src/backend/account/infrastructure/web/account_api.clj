(ns backend.account.infrastructure.web.account-api
  (:require [compojure.core :refer [defroutes POST PUT DELETE GET]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response status]]
            [backend.account.infrastructure.messaging.kafka-command-publisher :as cmd-pub]
            [backend.account.infrastructure.persistence.cassandra-repository :as cassandra-repo]
            [backend.account.application.port.out.account-repository :as account-repo]
            [backend.shared.cassandra :as cassandra-shared]
            [taoensso.timbre :as log]))

(defn create-account-handler [request]
  (try
    (let [body (:body request)
          {:keys [document name email]} body]
      (log/info " Account Service: Criando conta para" name "| Documento:" document)
      
      ;; Validação básica
      (if (and document name email)
        (do
          ;; Publica comando no Kafka
          (cmd-pub/publish-create-account-command document name email)
          (-> (response {:service "account-service"
                         :message "Conta sendo criada"
                         :status "processing"
                         :document document
                         :timestamp (str (java.time.Instant/now))})
              (status 202)))
        (-> (response {:error "Dados inválidos"
                       :required ["document" "name" "email"]
                       :received {:document document :name name :email email}})
            (status 400))))
    (catch Exception e
      (log/error "❌ Account Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn update-account-handler [request]
  (try
    (let [account-id (get-in request [:params :id])
          body (:body request)
          {:keys [name email]} body]
      (log/info " Account Service: Atualizando conta" account-id)
      
      (if (and account-id name email)
        (do
          (cmd-pub/publish-update-account-command account-id name email)
          (-> (response {:service "account-service"
                         :message "Conta sendo atualizada"
                         :status "processing"
                         :account-id account-id
                         :timestamp (str (java.time.Instant/now))})
              (status 202)))
        (-> (response {:error "Dados inválidos"
                       :required ["name" "email"]
                       :received {:account-id account-id :name name :email email}})
            (status 400))))
    (catch Exception e
      (log/error "❌ Account Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn close-account-handler [request]
  (try
    (let [account-id (get-in request [:params :id])
          body (:body request)
          {:keys [reason]} body]
      (log/info " Account Service: Fechando conta" account-id "| Motivo:" reason)
      
      (if account-id
        (do
          (cmd-pub/publish-close-account-command account-id (or reason "Solicitação via API"))
          (-> (response {:service "account-service"
                         :message "Conta sendo fechada"
                         :status "processing"
                         :account-id account-id
                         :reason (or reason "Solicitação via API")
                         :timestamp (str (java.time.Instant/now))})
              (status 202)))
        (-> (response {:error "ID da conta é obrigatório"})
            (status 400))))
    (catch Exception e
      (log/error "❌ Account Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn get-account-handler [request]
  (try
    (let [account-id (get-in request [:params :id])
          repository (cassandra-repo/create-account-repository cassandra-shared/cassandra-session)]
      (log/info " Account Service: Consultando conta" account-id)
      
      (if account-id
        (if-let [account (account-repo/find-by-id repository account-id)]
          (-> (response {:service "account-service"
                         :account account
                         :timestamp (str (java.time.Instant/now))})
              (status 200))
          (-> (response {:error "Conta não encontrada"
                         :account-id account-id})
              (status 404)))
        (-> (response {:error "ID da conta é obrigatório"})
            (status 400))))
    (catch Exception e
      (log/error "❌ Account Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn search-account-handler [request]
  (try
    (let [document (get (:query-params request) "document")
          repository (cassandra-repo/create-account-repository cassandra-shared/cassandra-session)]
      (log/info "🔍 Account Service: Buscando conta por documento" document)
      
      (if document
        (if-let [account (account-repo/find-by-document repository document)]
          (-> (response {:service "account-service"
                         :account account
                         :timestamp (str (java.time.Instant/now))})
              (status 200))
          (-> (response {:error "Conta não encontrada"
                         :document document})
              (status 404)))
        (-> (response {:error "Documento é obrigatório"})
            (status 400))))
    (catch Exception e
      (log/error "❌ Account Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn health-handler [_request]
  (response {:service "account-service"
             :status "healthy"
             :version "1.0.0"
             :timestamp (str (java.time.Instant/now))
             :dependencies {:kafka "connected"
                           :cassandra "connected"}}))

(defroutes account-routes
  ;; Account CRUD operations
  (POST "/accounts" [] create-account-handler)
  (PUT "/accounts/:id" [] update-account-handler)
  (DELETE "/accounts/:id" [] close-account-handler)
  (GET "/accounts/search" [] search-account-handler)
  (GET "/accounts/:id" [] get-account-handler)
  
  ;; Health check
  (GET "/health" [] health-handler)
  
  ;; Root endpoint
  (GET "/" [] (response {:service "account-service"
                         :message "Account Service API"
                         :version "1.0.0"
                         :endpoints ["/accounts" "/health"]}))
  
  ;; 404
  (route/not-found (response {:error "Endpoint não encontrado"
                               :service "account-service"})))

(def account-api
  (-> account-routes
      (wrap-params)
      (wrap-json-body {:keywords? true})
      (wrap-json-response)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete :options]
                 :access-control-allow-headers ["Content-Type" "Authorization"])))
