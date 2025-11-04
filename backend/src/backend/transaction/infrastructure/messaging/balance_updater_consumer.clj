(ns backend.transaction.infrastructure.messaging.balance-updater-consumer
  (:require [kafka-metamorphosis.consumer :as kafka]
            [backend.shared.cassandra :as cassandra-shared]
            [qbits.alia :as alia]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [mount.core :refer [defstate]]))

(def ^:const TRANSACTION_EVENTS_TOPIC "transaction-events")

(def ^:const CONSUMER_CONFIG
  {:bootstrap-servers "localhost:9092"
   :group-id "balance-updater-group"
   :key-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :value-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :auto-offset-reset "earliest"})

(defn update-account-balance [session account-id amount-change]
  (try
    (log/info "💰 Updating account balance" account-id "| Change:" amount-change)
    
    ;; Fetch current balance
    (let [result (first (alia/execute 
                          session
                          "SELECT balance_amount FROM accounts WHERE id = ?"
                          {:values [account-id]}))
          current-balance (or (:balance_amount result) 0)
          new-balance (+ current-balance amount-change)]
      
      (log/info "   Current balance:" current-balance "→ New balance:" new-balance)
      
      ;; Update balance
      (alia/execute
        session
        "UPDATE accounts SET balance_amount = ? WHERE id = ?"
        {:values [new-balance account-id]})
      
      (log/info "✅ Balance updated successfully"))
    (catch Exception e
      (log/error "❌ Error updating balance:" (.getMessage e)))))

#_{:clj-kondo/ignore [:unresolved-var]}
(defn handle-transaction-event [message]
  (try
    (let [event (json/parse-string (:value message) true)
          event-type (:type event)
          transaction (:data event)]
      
      (log/info "📬 Evento:" event-type "| Offset:" (:offset message))
      
      (when (= event-type "transaction-created")
        (let [tx-type (keyword (:type transaction))
              amount (:amount transaction)
              from-account (:from-account-id transaction)
              to-account (:to-account-id transaction)]
          
          (case tx-type
            :deposit
            (do
              (log/info "💵 Processing deposit of $" amount "to" to-account)
              (update-account-balance cassandra-shared/cassandra-session to-account amount))
            
            :withdrawal
            (do
              (log/info "💸 Processing withdrawal of $" amount "from" from-account)
              (update-account-balance cassandra-shared/cassandra-session from-account (- amount)))
            
            :transfer
            (do
              (log/info "💱 Processing transfer of $" amount "from" from-account "to" to-account)
              (update-account-balance cassandra-shared/cassandra-session from-account (- amount))
              (update-account-balance cassandra-shared/cassandra-session to-account amount))
            
            (log/warn "⚠️ Unknown transaction type:" tx-type)))))
    (catch Exception e
      (log/error "❌ Error processing event:" (.getMessage e))
      nil)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate balance-updater-consumer
  :start
  (let [consumer (kafka/create CONSUMER_CONFIG)]
    (log/info "🚀 Starting balance-updater consumer...")
    (kafka/subscribe! consumer [TRANSACTION_EVENTS_TOPIC])
    (future (kafka/consume! consumer {:poll-timeout 1000
                                      :handler handle-transaction-event}))
    consumer)
  :stop
  (do
    (log/info "🛑 Stopping balance-updater consumer...")
    (kafka/close! balance-updater-consumer)))
