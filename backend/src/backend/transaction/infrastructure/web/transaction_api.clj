(ns backend.transaction.infrastructure.web.transaction-api
  (:require [compojure.core :refer [defroutes POST GET]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response status]]
            [backend.transaction.infrastructure.messaging.kafka-command-publisher :as cmd-pub]
            [backend.transaction.infrastructure.persistence.cassandra-repository :as cassandra-repo]
            [backend.transaction.application.port.out.transaction-repository :as transaction-repo]
            [backend.shared.cassandra :as cassandra-shared]
            [taoensso.timbre :as log]))

(defn transfer-handler [request]
  (try
    (let [body (:body request)
          {:keys [from-account-id to-account-id amount description]} body]
      (log/info "�� Transaction Service: Transferindo de" from-account-id "para" to-account-id "valor:" amount)
      
      (if (and from-account-id to-account-id amount)
        (do
          (cmd-pub/publish-transfer-command from-account-id to-account-id amount description)
          (-> (response {:service "transaction-service"
                         :message "Transferência sendo processada"
                         :status "processing"
                         :from-account-id from-account-id
                         :to-account-id to-account-id
                         :amount amount
                         :timestamp (str (java.time.Instant/now))})
              (status 202)))
        (-> (response {:error "Dados inválidos"
                       :required ["from-account-id" "to-account-id" "amount"]
                       :received {:from-account-id from-account-id 
                                 :to-account-id to-account-id 
                                 :amount amount}})
            (status 400))))
    (catch Exception e
      (log/error "❌ Transaction Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn deposit-handler [request]
  (try
    (let [body (:body request)
          {:keys [account-id amount description]} body]
      (log/info "�� Transaction Service: Depositando na conta" account-id "valor:" amount)
      
      (if (and account-id amount)
        (do
          (cmd-pub/publish-deposit-command account-id amount description)
          (-> (response {:service "transaction-service"
                         :message "Depósito sendo processado"
                         :status "processing"
                         :account-id account-id
                         :amount amount
                         :timestamp (str (java.time.Instant/now))})
              (status 202)))
        (-> (response {:error "Dados inválidos"
                       :required ["account-id" "amount"]
                       :received {:account-id account-id :amount amount}})
            (status 400))))
    (catch Exception e
      (log/error "❌ Transaction Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn withdrawal-handler [request]
  (try
    (let [body (:body request)
          {:keys [account-id amount description]} body]
      (log/info "💸 Transaction Service: Sacando da conta" account-id "valor:" amount)
      
      (if (and account-id amount)
        (do
          (cmd-pub/publish-withdrawal-command account-id amount description)
          (-> (response {:service "transaction-service"
                         :message "Saque sendo processado"
                         :status "processing"
                         :account-id account-id
                         :amount amount
                         :timestamp (str (java.time.Instant/now))})
              (status 202)))
        (-> (response {:error "Dados inválidos"
                       :required ["account-id" "amount"]
                       :received {:account-id account-id :amount amount}})
            (status 400))))
    (catch Exception e
      (log/error "❌ Transaction Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn get-transactions-handler [request]
  (try
    (let [account-id (get (:query-params request) "account-id")
          repository (cassandra-repo/create-transaction-repository cassandra-shared/cassandra-session)]
      (log/info "📋 Transaction Service: Buscando transações da conta" account-id)
      
      (if account-id
        (let [transactions (transaction-repo/find-by-account repository account-id)]
          (-> (response {:service "transaction-service"
                         :transactions transactions
                         :count (count transactions)
                         :timestamp (str (java.time.Instant/now))})
              (status 200)))
        (-> (response {:error "account-id é obrigatório"})
            (status 400))))
    (catch Exception e
      (log/error "❌ Transaction Service Error:" (.getMessage e))
      (-> (response {:error "Erro interno do servidor"
                     :details (.getMessage e)})
          (status 500)))))

(defn health-handler [_request]
  (response {:service "transaction-service"
             :status "healthy"
             :version "1.0.0"
             :timestamp (str (java.time.Instant/now))
             :dependencies {:kafka "connected"
                           :cassandra "connected"}}))

(defroutes transaction-routes
  ;; Transaction operations
  (POST "/transactions/transfer" [] transfer-handler)
  (POST "/transactions/deposit" [] deposit-handler)
  (POST "/transactions/withdrawal" [] withdrawal-handler)
  (GET "/transactions" [] get-transactions-handler)
  
  ;; Health check
  (GET "/health" [] health-handler)
  
  ;; Root endpoint
  (GET "/" [] (response {:service "transaction-service"
                         :message "Transaction Service API"
                         :version "1.0.0"
                         :endpoints ["/transactions/transfer" 
                                    "/transactions/deposit" 
                                    "/transactions/withdrawal" 
                                    "/transactions" 
                                    "/health"]}))
  
  ;; 404
  (route/not-found (response {:error "Endpoint não encontrado"
                               :service "transaction-service"})))

(def transaction-api
  (-> transaction-routes
      (wrap-params)
      (wrap-json-body {:keywords? true})
      (wrap-json-response)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete :options]
                 :access-control-allow-headers ["Content-Type" "Authorization"])))
