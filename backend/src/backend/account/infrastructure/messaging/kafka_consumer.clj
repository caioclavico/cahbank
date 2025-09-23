(ns backend.account.infrastructure.messaging.kafka-consumer
  (:require [kafka-metamorphosis.consumer :as kafka]
            [kafka-metamorphosis.core :as kafka-core]
            [backend.account.application.service :as app-service]
            [backend.account.application.port.in.account-service :as account-service]
            [backend.account.infrastructure.persistence.in-memory-repository :as in-mem-repo]
            [backend.account.infrastructure.messaging.kafka-event-publisher :as event-pub]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [mount.core :refer [defstate]]))

(def ^:const ACCOUNT_COMMANDS_TOPIC "account-cmds")

(def ^:const CONSUMER_CONFIG
  {:bootstrap-servers "localhost:9092"
   :group-id "account-cmds-group"
   :key-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :value-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :auto-offset-reset "earliest"})

(def ^:private account-service-instance
  (let [repository (in-mem-repo/create-in-memory-account-repository)
        event-publisher (event-pub/create-event-publisher)]
    (app-service/create-account-service repository event-publisher)))

(defn handle-account-cmd [message]
  (try
    (let [command (json/parse-string (:value message) true)
          command-type (:type command)]
      (log/info "Comando recebido - Tipo:" command-type "Offset:" (:offset message))
      (case command-type
        "create-account" 
        (do
          (log/info "Criando conta com dados:" (:data command))
          (let [{:keys [document name email]} (:data command)]
            (account-service/open-account account-service-instance document name email)))
        
        "update-account"
        (do
          (log/info "Atualizando conta com dados:" (:data command))
          (let [{:keys [account-id name email]} (:data command)]
            (account-service/update-account account-service-instance account-id name email)))
        
        "close-account"
        (do
          (log/info "Fechando conta com dados:" (:data command))
          (let [{:keys [account-id reason]} (:data command)]
            (account-service/close-account account-service-instance account-id reason)))
        
        (log/warn "Comando desconhecido:" command-type)))

    (catch Exception e
      (log/error "Erro ao processar comando:" (.getMessage e))
      nil)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate kafka-consumer
  :start
  (let [consumer (kafka/create CONSUMER_CONFIG)]
    (log/info ">>> Iniciando consumer de account-cmds...")
    (println (kafka-core/health-check))
    (kafka/subscribe! consumer [ACCOUNT_COMMANDS_TOPIC])
    (kafka/consume! consumer {:poll-timeout 1000
                              :handler handle-account-cmd})
    consumer)
  :stop
  (do
    (log/info ">>> Parando consumer de account-cmds...")
    (kafka/close! kafka-consumer)))