(ns backend.transaction.infrastructure.messaging.kafka-consumer
  (:require [kafka-metamorphosis.consumer :as kafka]
            [kafka-metamorphosis.core :as kafka-core]
            [backend.transaction.application.service :as app-service]
            [backend.transaction.application.port.in.transaction-service :as transaction-service]
            [backend.transaction.infrastructure.persistence.cassandra-repository :as cassandra-repo]
            [backend.transaction.infrastructure.messaging.kafka-event-publisher :as event-pub]
            [backend.shared.cassandra :as cassandra-shared]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [mount.core :refer [defstate]]))

(def ^:const TRANSACTION_COMMANDS_TOPIC "transaction-cmds")

(def ^:const CONSUMER_CONFIG
  {:bootstrap-servers "localhost:9092"
   :group-id "transaction-cmds-group"
   :key-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :value-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :auto-offset-reset "earliest"})

(defn handle-transaction-cmd [message]
  (try
    (let [command (json/parse-string (:value message) true)
          command-type (:type command)]
      (log/info "📨 Comando:" command-type "| Offset:" (:offset message))
      (case command-type
        "transfer" 
        (let [{:keys [from-account-id to-account-id amount description]} (:data command)
              transaction-service-instance (let [repository (cassandra-repo/create-transaction-repository @cassandra-shared/cassandra-session)
                                                 event-publisher (event-pub/create-event-publisher)]
                                           (app-service/create-transaction-service repository event-publisher))]
          (transaction-service/transfer transaction-service-instance from-account-id to-account-id amount description))
        
        "deposit"
        (let [{:keys [account-id amount description]} (:data command)
              transaction-service-instance (let [repository (cassandra-repo/create-transaction-repository @cassandra-shared/cassandra-session)
                                                 event-publisher (event-pub/create-event-publisher)]
                                           (app-service/create-transaction-service repository event-publisher))]
          (transaction-service/deposit transaction-service-instance account-id amount description))
        
        "withdrawal"
        (let [{:keys [account-id amount description]} (:data command)
              transaction-service-instance (let [repository (cassandra-repo/create-transaction-repository @cassandra-shared/cassandra-session)
                                                 event-publisher (event-pub/create-event-publisher)]
                                           (app-service/create-transaction-service repository event-publisher))]
          (transaction-service/withdraw transaction-service-instance account-id amount description))
        
        (log/warn "⚠️ Comando desconhecido:" command-type)))
    (catch Exception e
      (log/error "❌ Erro ao processar comando:" (.getMessage e))
      nil)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate kafka-consumer
  :start
  (let [consumer (kafka/create CONSUMER_CONFIG)]
    (log/info "🚀 Iniciando consumer de transaction-cmds...")
    (println (kafka-core/health-check))
    (kafka/subscribe! consumer [TRANSACTION_COMMANDS_TOPIC])
    (kafka/consume! consumer {:poll-timeout 1000
                              :handler handle-transaction-cmd})
    consumer)
  :stop
  (do
    (log/info "🛑 Parando consumer de transaction-cmds...")
    (kafka/close! kafka-consumer)))