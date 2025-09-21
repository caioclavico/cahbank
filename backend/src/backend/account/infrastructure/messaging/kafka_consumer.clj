(ns backend.account.infrastructure.messaging.kafka-consumer
  (:require [kafka-metamorphosis.consumer :as kafka]
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

(defn handle-account-cmd [message]
  (try
    (let [command (json/parse-string (:value message) true)
          command-type (:type command)]
      (log/info "Comando recebido - Tipo:" command-type "Offset:" (:offset message)))

    (catch Exception e
      (println "Error processing command:" (.getMessage e)))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate kafka-consumer
  :start
  (let [consumer (kafka/create CONSUMER_CONFIG)]
    (log/info ">>> Iniciando consumer de account-cmds...")
    (kafka/subscribe! consumer [ACCOUNT_COMMANDS_TOPIC])
    (kafka/consume! consumer {:poll-timeout 1000
                              :handler handle-account-cmd})
    consumer)
  :stop
  (do
    (println ">>> Parando consumer de account-cmds...")
    (kafka/stop-consumer kafka-consumer)))