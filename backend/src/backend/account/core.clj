(ns backend.account.core
  (:require [mount.core :as mount]
            [backend.account.infrastructure.messaging.kafka-consumer]
            [backend.shared.cassandra]
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
  (log/info "🚀 Starting Account Service...")
  (run-jetty account-api {:port 8081 :join? false})
  (log/info "🌐 Account Service API available at: http://localhost:8081")
  (future (mount/start)))

(defn stop-account-service []
  (log/info "🛑 Stopping Account Service...")
  (mount/stop))

(defn -main [& _args]
  (start-account-service))
