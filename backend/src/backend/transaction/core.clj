(ns backend.transaction.core
  (:require [mount.core :as mount]
            [backend.transaction.infrastructure.messaging.kafka-consumer]
            [backend.shared.cassandra]
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
    (log/info "🚀 Starting Transaction Service...")
    (run-jetty transaction-api {:port port :join? false})
    (log/info "🌐 Transaction Service API available at: http://localhost:" port)
    (future (mount/start))))

(defn stop-transaction-service []
  (log/info "🛑 Stopping Transaction Service...")
  (mount/stop))

(defn -main [& _args]
  (start-transaction-service))
