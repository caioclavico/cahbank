(ns backend.transaction.infrastructure.messaging.kafka-command-publisher
  (:require [kafka-metamorphosis.producer :as kafka]
            [taoensso.timbre :as log]
            [cheshire.core :as json]))

(def ^:const TRANSACTION_COMMANDS_TOPIC "transaction-cmds")

(def ^:const PRODUCER_CONFIG
  {:bootstrap-servers "localhost:9092"
   :key-serializer "org.apache.kafka.common.serialization.StringSerializer"
   :value-serializer "org.apache.kafka.common.serialization.StringSerializer"})

(defn- create-producer []
  (kafka/create PRODUCER_CONFIG))

(defn publish-transfer-command [from-account-id to-account-id amount description]
  (let [producer (create-producer)
        command {:type "transfer"
                 :id (str (java.util.UUID/randomUUID))
                 :data {:from-account-id from-account-id
                        :to-account-id to-account-id
                        :amount amount
                        :description description}
                 :timestamp (str (java.time.Instant/now))
                 :version 1}
        command-json (json/generate-string command)]
    (kafka/send! producer TRANSACTION_COMMANDS_TOPIC (:id command) command-json)
    (log/info "�� Comando transfer publicado:" (:id command))
    (kafka/close! producer)))

(defn publish-deposit-command [account-id amount description]
  (let [producer (create-producer)
        command {:type "deposit"
                 :id (str (java.util.UUID/randomUUID))
                 :data {:account-id account-id
                        :amount amount
                        :description description}
                 :timestamp (str (java.time.Instant/now))
                 :version 1}
        command-json (json/generate-string command)]
    (kafka/send! producer TRANSACTION_COMMANDS_TOPIC (:id command) command-json)
    (log/info "�� Comando deposit publicado:" (:id command))
    (kafka/close! producer)))

(defn publish-withdrawal-command [account-id amount description]
  (let [producer (create-producer)
        command {:type "withdrawal"
                 :id (str (java.util.UUID/randomUUID))
                 :data {:account-id account-id
                        :amount amount
                        :description description}
                 :timestamp (str (java.time.Instant/now))
                 :version 1}
        command-json (json/generate-string command)]
    (kafka/send! producer TRANSACTION_COMMANDS_TOPIC (:id command) command-json)
    (log/info "💸 Comando withdrawal publicado:" (:id command))
    (kafka/close! producer)))
