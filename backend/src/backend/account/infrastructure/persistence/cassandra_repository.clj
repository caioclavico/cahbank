(ns backend.account.infrastructure.persistence.cassandra-repository
  (:require [backend.account.application.port.out.account-repository :as account-repo]
            [qbits.alia :as alia]
            [qbits.hayt :refer [insert select where values]]))

(defrecord CassandraAccountRepository [session]
  account-repo/AccountRepository

  (save-account [_this account]
    (alia/execute
     session
     (insert :accounts
             (values {:id (:id account)
                      :document (:document account)
                      :name (:name account)
                      :email (:email account)
                      :balance_amount (get-in account [:balance :amount])
                      :balance_currency (get-in account [:balance :currency])
                      :status (name (:status account))
                      :created_at (:created-at account)})))
    account)

  (get-account [_this account-id]
    (first
     (alia/execute
      session
      (select :accounts
              (where [[= :id account-id]]))))))

(defn create-account-repository [session]
  (->CassandraAccountRepository session))