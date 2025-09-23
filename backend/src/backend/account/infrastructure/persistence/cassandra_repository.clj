(ns backend.account.infrastructure.persistence.cassandra-repository
  (:require [backend.account.application.port.out.account-repository :as account-repo]
            [qbits.alia :as alia]
            [qbits.hayt :refer [insert select where values limit]]))

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
              (where [[= :id account-id]])))))

  (exists-by-document? [_this document]
    (boolean
     (alia/execute
      session
      (select :accounts
              (where [[= :document document]])
              (limit 1)))))

  (find-by-document [_this document]
    (first
     (alia/execute
      session
      (select :accounts
              (where [[= :document document]])))))

  (find-by-id [_this account-id]
    (first
     (alia/execute
      session
      (select :accounts
              (where [[= :id account-id]]))))))

(defn create-account-repository [session]
  (->CassandraAccountRepository session))