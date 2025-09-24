(ns backend.transaction.domain.event)

(defn transaction-created-event
  "Cria evento de transação criada"
  [transaction]
  {:type "transaction-created"
   :id (:id transaction)
   :data {:transaction-id (:id transaction)
          :from-account-id (:from-account-id transaction)
          :to-account-id (:to-account-id transaction)
          :amount (:amount transaction)
          :type (:type transaction)
          :status (name (:status transaction))
          :description (:description transaction)
          :timestamp (str (:timestamp transaction))}
   :timestamp (str (java.time.Instant/now))
   :version 1})

(defn transaction-processed-event
  "Cria evento de transação processada"
  [transaction]
  {:type "transaction-processed"
   :id (str (java.util.UUID/randomUUID))
   :data {:transaction-id (:id transaction)
          :from-account-id (:from-account-id transaction)
          :to-account-id (:to-account-id transaction)
          :amount (:amount transaction)
          :type (:type transaction)
          :status (name (:status transaction))}
   :timestamp (str (java.time.Instant/now))
   :version 1})

(defn transaction-failed-event
  "Cria evento de transação falhada"
  [transaction reason]
  {:type "transaction-failed"
   :id (str (java.util.UUID/randomUUID))
   :data {:transaction-id (:id transaction)
          :from-account-id (:from-account-id transaction)
          :to-account-id (:to-account-id transaction)
          :amount (:amount transaction)
          :type (:type transaction)
          :reason reason}
   :timestamp (str (java.time.Instant/now))
   :version 1})
