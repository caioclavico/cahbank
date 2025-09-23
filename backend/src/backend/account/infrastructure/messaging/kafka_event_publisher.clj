(ns backend.account.infrastructure.messaging.kafka-event-publisher
  (:require [backend.account.application.port.out.event-publisher :as event-pub]
            [kafka-metamorphosis.producer :as kafka]
            [taoensso.timbre :as log]
            [cheshire.core :as json]))

(def ^:const ACCOUNT_EVENTS_TOPIC "account-events")

(def ^:const PRODUCER_CONFIG
  {:bootstrap-servers "localhost:9092"
   :key-serializer "org.apache.kafka.common.serialization.StringSerializer"
   :value-serializer "org.apache.kafka.common.serialization.StringSerializer"})

(defrecord KafkaEventPublisher [producer]
  event-pub/EventPublisher

  (publish-event [_this event]
    (try
      (let [event-json (json/generate-string event)]
        (kafka/send! producer ACCOUNT_EVENTS_TOPIC (:id event) event-json)
        (log/info "Evento publicado:" (:type event) "ID:" (:id event)))
      (catch Exception e
        (log/error "Erro ao publicar evento:" (.getMessage e))
        (throw e)))))

(defn create-event-publisher []
  (let [producer (kafka/create PRODUCER_CONFIG)]
    (->KafkaEventPublisher producer)))

