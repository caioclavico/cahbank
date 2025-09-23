(ns backend.account.domain.event)

(defn account-opened-event
  "Cria evento de conta aberta"
  [account]
  {:type "account-opened"
   :id (:id account)
   :data {:account-id (:id account)
          :document (:document account)
          :name (:name account)
          :email (:email account)
          :balance (:balance account)
          :status (name (:status account))
          :created-at (str (:created-at account))}
   :timestamp (str (java.time.Instant/now))
   :version 1})

(defn account-updated-event
  "Cria evento de atualização de conta"
  [account]
  {:type "account-updated"
   :id (str (java.util.UUID/randomUUID))
   :data {:account-id (:id account)
          :document (:document account)
          :name (:name account)
          :email (:email account)
          :status (name (:status account))}
   :timestamp (str (java.time.Instant/now))
   :version 1})

(defn account-closed-event
  "Cria evento de fechamento de conta"
  [account reason]
  {:type "account-closed"
   :id (str (java.util.UUID/randomUUID))
   :data {:account-id (:id account)
          :document (:document account)
          :reason reason}
   :timestamp (str (java.time.Instant/now))
   :version 1})
