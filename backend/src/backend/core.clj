(ns backend.core
  (:require [taoensso.timbre :as log]
            [mount.core :as mount]
            [backend.account.core]
            [backend.transaction.core]
            [backend.account.infrastructure.web.account-api :refer [account-api]]
            [backend.transaction.infrastructure.web.transaction-api :refer [transaction-api]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defonce account-server (atom nil))
(defonce transaction-server (atom nil))

(defn start-backend []
  (log/info "🚀 Starting Backend Services...") 
  (log/info "🌐 Starting Account Service API on port 8081...")

  (reset! account-server (run-jetty account-api {:port 8081 :join? false}))
  (Thread/sleep 500)

  (log/info "✅ Account Service available at: http://localhost:8081")
  (log/info "🌐 Starting Transaction Service API on port 8082...")

  (reset! transaction-server (run-jetty transaction-api {:port 8082 :join? false}))
  (Thread/sleep 500)

  (log/info "✅ Transaction Service available at: http://localhost:8082")
  (log/info "🔌 Starting Cassandra and Kafka consumers...")

  (future (mount/start))
  (log/info "🎉 Backend services started successfully!"))

(defn stop-backend []
  (log/info "🛑 Stopping Backend Services...")
  
  (when @account-server
    (log/info "Stopping Account Service...")
    (.stop @account-server)
    (reset! account-server nil))
  
  (when @transaction-server
    (log/info "Stopping Transaction Service...")
    (.stop @transaction-server)
    (reset! transaction-server nil))
  
  (mount/stop)
  
  (log/info "✅ Backend services stopped successfully!"))

;; ========================================
;; Controle Individual de Serviços
;; ========================================

(defn start-account-only []
  (log/info "🚀 Starting Account Service ONLY...")
  (log/info "🌐 Starting Account Service API on port 8081...")
  (reset! account-server (run-jetty account-api {:port 8081 :join? false}))
  (Thread/sleep 500)
  (log/info "✅ Account Service available at: http://localhost:8081")
  (log/info "🔌 Starting Cassandra and consumers...")
  (future (mount/start))
  (log/info "🎉 Account Service started!"))

(defn stop-account-only []
  (log/info "🛑 Stopping Account Service ONLY...")
  (when @account-server
    (.stop @account-server)
    (reset! account-server nil))
  (mount/stop)
  (log/info "✅ Account Service stopped!"))

(defn start-transaction-only []
  (log/info "🚀 Starting Transaction Service ONLY...")
  (log/info "🌐 Starting Transaction Service API on port 8082...")
  (reset! transaction-server (run-jetty transaction-api {:port 8082 :join? false}))
  (Thread/sleep 500)
  (log/info "✅ Transaction Service available at: http://localhost:8082") 
  (log/info "🔌 Starting Cassandra and consumers...")
  (future (mount/start))
  (log/info "🎉 Transaction Service started!"))

(defn stop-transaction-only []
  (log/info "🛑 Stopping Transaction Service ONLY...")
  (when @transaction-server
    (.stop @transaction-server)
    (reset! transaction-server nil))
  (mount/stop)
  (log/info "✅ Transaction Service stopped!"))

;; ========================================
;; Main Functions
;; ========================================

(defn -main 
  "Starts backend services.
  
  Usage:
    lein run              - Start both services (account + transaction)
    lein run account      - Start only Account Service (port 8081)
    lein run transaction  - Start only Transaction Service (port 8082)"
  [& args]
  (let [service (first args)]
    (case service
      "account" 
      (do
        (start-account-only)
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. #(do
                                      (log/info "🛑 Received shutdown signal...")
                                      (stop-account-only))))
        (log/info "📌 Account Service running on port 8081")
        (log/info "📌 Stop: Ctrl+C")
        @(promise))
      
      "transaction"
      (do
        (start-transaction-only)
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. #(do
                                      (log/info "🛑 Received shutdown signal...")
                                      (stop-transaction-only))))
        (log/info "📌 Transaction Service running on port 8082")
        (log/info "📌 Stop: Ctrl+C")
        @(promise))
      
      ;; Default: start both
      (do
        (start-backend)
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. #(do
                                      (log/info "🛑 Received shutdown signal...")
                                      (stop-backend))))
        (log/info "📌 Backend services running...")
        (log/info "📌 Leiningen: Ctrl+C | Docker: docker-compose down")
        @(promise)))))