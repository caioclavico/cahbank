(ns backend.account.core
  (:require [mount.core :as mount]
            [backend.account.infrastructure.messaging.kafka-consumer :refer [kafka-consumer]]
            [backend.shared.cassandra :refer [cassandra-session]]
            [backend.account.infrastructure.web.account-api :refer [account-api]]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]))

(defn configure-logging []
  (log/merge-config!
   {:appenders {:println {:enabled? true}
                :spit (appenders/spit-appender {:fname "logs/account-service.log"})}}))

(defn start-account-service []
  (configure-logging)
  (log/info "🚀 Iniciando Account Service...")
  (run-jetty account-api {:port 8081 :join? false})
  (log/info "🌐 Account Service API disponível em: http://localhost:8081")
  (mount/start))

(defn stop-account-service []
  (log/info " Parando Account Service...")
  (mount/stop))

(defn -main [& _args]
  (start-account-service))
