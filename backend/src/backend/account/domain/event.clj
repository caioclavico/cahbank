(ns backend.account.domain.event)

(defrecord AccountOpened [account-id document name email timestamp])

(defn account-opened-event
  "Cria evento de conta aberta"
  [account]
  (->AccountOpened (:id account)
                   (:document account)
                   (:name account)
                   (:email account)
                   (java.time.Instant/now)))
