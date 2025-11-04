(ns backend.account.infrastructure.messaging.kafka-consumer
  (:require [kafka-metamorphosis.consumer :as kafka]
            [kafka-metamorphosis.core :as kafka-core]
            [backend.account.application.service :as app-service]
            [backend.account.application.port.in.account-service :as account-service]
            [backend.account.infrastructure.persistence.cassandra-repository :as cassandra-repo]
            [backend.account.infrastructure.messaging.kafka-event-publisher :as event-pub]
            [backend.shared.cassandra :as cassandra-shared]
            [backend.shared.kafka-config :as kafka-config]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [mount.core :refer [defstate]]))

(def ^:const ACCOUNT_COMMANDS_TOPIC "account-cmds")

(def CONSUMER_CONFIG (kafka-config/consumer-config "account-cmds-group"))

(defn handle-account-cmd [message]
  (try
    (let [command (json/parse-string (:value message) true)
          command-type (:type command)]
      (log/info "📨 Comando:" command-type "| Offset:" (:offset message))
      (case command-type
        "create-account" 
        (let [{:keys [document name email]} (:data command)
              account-service-instance (let [repository (cassandra-repo/create-account-repository cassandra-shared/cassandra-session)
                                             event-publisher (event-pub/create-event-publisher)]
                                        (app-service/create-account-service repository event-publisher))]
          (account-service/open-account account-service-instance document name email))
        
        "update-account"
        (let [{:keys [account-id name email]} (:data command)
              account-service-instance (let [repository (cassandra-repo/create-account-repository cassandra-shared/cassandra-session)
                                             event-publisher (event-pub/create-event-publisher)]
                                        (app-service/create-account-service repository event-publisher))]
          (account-service/update-account account-service-instance account-id name email))
        
        "close-account"
        (let [{:keys [account-id reason]} (:data command)
              account-service-instance (let [repository (cassandra-repo/create-account-repository cassandra-shared/cassandra-session)
                                             event-publisher (event-pub/create-event-publisher)]
                                        (app-service/create-account-service repository event-publisher))]
          (account-service/close-account account-service-instance account-id reason))
        
        (log/warn "⚠️ Comando desconhecido:" command-type)))
    (catch Exception e
      (log/error "❌ Erro ao processar comando:" (.getMessage e))
      nil)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate kafka-consumer
  :start
  (let [consumer (kafka/create CONSUMER_CONFIG)]
    (log/info "🚀 Starting account-cmds consumer...")
    (println (kafka-core/health-check))
    (kafka/subscribe! consumer [ACCOUNT_COMMANDS_TOPIC])
    (future (kafka/consume! consumer {:poll-timeout 1000
                                      :handler handle-account-cmd})) 
    consumer)
    :stop
    (do
      (log/info "🛑 Stopping account-cmds consumer...")
      (kafka/close! kafka-consumer)))