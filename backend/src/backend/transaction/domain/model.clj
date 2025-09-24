(ns backend.transaction.domain.model
  (:import [java.util UUID]))

(defrecord Transaction [id from-account-id to-account-id amount type status description timestamp])

(defrecord Transfer [id from-account-id to-account-id amount description])

(defrecord Deposit [id account-id amount description])

(defrecord Withdrawal [id account-id amount description])

(defn create-transfer
  "Cria uma transferência entre contas"
  [from-account-id to-account-id amount description]
  (->Transfer (str (UUID/randomUUID))
              from-account-id
              to-account-id
              amount
              (or description "Transferência")))

(defn create-deposit
  "Cria um depósito"
  [account-id amount description]
  (->Deposit (str (UUID/randomUUID))
             account-id
             amount
             (or description "Depósito")))

(defn create-withdrawal
  "Cria um saque"
  [account-id amount description]
  (->Withdrawal (str (UUID/randomUUID))
                account-id
                amount
                (or description "Saque")))

(defn valid-amount?
  "Valida se o valor é positivo"
  [amount]
  (and (number? amount)
       (> amount 0)))

(defn can-transfer?
  "Valida se pode fazer transferência"
  [from-account-id to-account-id amount]
  (and (not= from-account-id to-account-id)
       (valid-amount? amount)
       (string? from-account-id)
       (string? to-account-id)))

(defn can-deposit?
  "Valida se pode fazer depósito"
  [account-id amount]
  (and (string? account-id)
       (valid-amount? amount)))

(defn can-withdraw?
  "Valida se pode fazer saque"
  [account-id amount]
  (and (string? account-id)
       (valid-amount? amount)))
