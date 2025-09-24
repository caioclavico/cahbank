(ns backend.account.infrastructure.messaging.kafka-command-publisher
  (:require [kafka-metamorphosis.producer :as kafka]
            [taoensso.timbre :as log]
            [cheshire.core :as json]))

(def ^:const ACCOUNT_COMMANDS_TOPIC "account-cmds")

(def ^:const PRODUCER_CONFIG
  {:bootstrap-servers "localhost:9092"
   :key-serializer "org.apache.kafka.common.serialization.StringSerializer"
   :value-serializer "org.apache.kafka.common.serialization.StringSerializer"})

(defn- create-producer []
  (kafka/create PRODUCER_CONFIG))

(defn publish-create-account-command [document name email]
  (let [producer (create-producer)
        command {:type "create-account"
                 :id (str (java.util.UUID/randomUUID))
                 :data {:document document
                        :name name
                        :email email}
                 :timestamp (str (java.time.Instant/now))
                 :version 1}
        command-json (json/generate-string command)]
    (kafka/send! producer ACCOUNT_COMMANDS_TOPIC (:id command) command-json)
    (log/info " Comando create-account publicado:" (:id command))
    (kafka/close! producer)))

(defn publish-update-account-command [account-id name email]
  (let [producer (create-producer)
        command {:type "update-account"
                 :id (str (java.util.UUID/randomUUID))
                 :data {:account-id account-id
                        :name name
                        :email email}
                 :timestamp (str (java.time.Instant/now))
                 :version 1}
        command-json (json/generate-string command)]
    (kafka/send! producer ACCOUNT_COMMANDS_TOPIC (:id command) command-json)
    (log/info " Comando update-account publicado:" (:id command))
    (kafka/close! producer)))

(defn publish-close-account-command [account-id reason]
  (let [producer (create-producer)
        command {:type "close-account"
                 :id (str (java.util.UUID/randomUUID))
                 :data {:account-id account-id
                        :reason reason}
                 :timestamp (str (java.time.Instant/now))
                 :version 1}
        command-json (json/generate-string command)]
    (kafka/send! producer ACCOUNT_COMMANDS_TOPIC (:id command) command-json)
    (log/info " Comando close-account publicado:" (:id command))
    (kafka/close! producer)))