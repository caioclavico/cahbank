(ns backend.transaction.core
  (:require [mount.core :as mount]
            [backend.transaction.infrastructure.messaging.kafka-consumer :refer [kafka-consumer]]
            [backend.shared.cassandra :refer [cassandra-session]]
            [backend.transaction.infrastructure.web.transaction-api :refer [transaction-api]]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]))

(defn configure-logging []
  (log/merge-config!
   {:appenders {:println {:enabled? true}
                :spit (appenders/spit-appender {:fname "logs/transaction-service.log"})}}))

(defn get-port []
  (or (some-> (System/getenv "SERVICE_PORT") Integer/parseInt)
      8082)) ; Porta padrão para Transaction Service

(defn start-transaction-service []
  (configure-logging)
  (let [port (get-port)]
    (log/info "🚀 Iniciando Transaction Service...")
    (mount/start)
    (log/info "🌐 Transaction Service API disponível em: http://localhost:" port)
    (run-jetty transaction-api {:port port :join? false})
    (Thread/sleep 1000)
    (log/info "✅ Servidor iniciado com sucesso na porta" port)
    (while true (Thread/sleep 1000))))

(defn stop-transaction-service []
  (log/info "🛑 Parando Transaction Service...")
  (mount/stop))

(defn -main [& _args]
  (start-transaction-service))
