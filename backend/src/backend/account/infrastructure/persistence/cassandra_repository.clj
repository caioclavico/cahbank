(ns backend.account.infrastructure.persistence.cassandra-repository
  (:require [backend.account.application.port.out.account-repository :as account-repo]
            [qbits.alia :as alia]))

(defrecord CassandraAccountRepository [session]
  account-repo/AccountRepository

  (save-account [_this account]
    (alia/execute
       session
       (str "INSERT INTO accounts (id, document, name, email, balance_amount, balance_currency, status, created_at) "
            "VALUES ('" (:id account) "', '"
            (:document account) "', '"
            (:name account) "', '"
            (:email account) "', "
            (:amount (:balance account)) ", '"
            (:currency (:balance account)) "', '"
            (name (:status account)) "', '"
            (:created-at account) "')")))

  (exists-by-document? [_this document]
    (boolean
     (first
      (alia/execute
       session
       (str "SELECT id FROM accounts WHERE document = ? LIMIT 1")
       {:values [document]}))))

  (find-by-document [_this document]
    (first
     (alia/execute
      session
      (str "SELECT * FROM accounts WHERE document = ?")
      {:values [document]})))

  (find-by-id [_this account-id]
    (first
     (alia/execute
      session
      (str "SELECT * FROM accounts WHERE id = ?")
      {:values [account-id]})))

  (update-account [_this account]
    (alia/execute
     session
     (str "UPDATE accounts SET name = ?, email = ?, balance_amount = ?, balance_currency = ?, status = ? WHERE id = ?")
     {:values [(:name account)
               (:email account)
               (:amount (:balance account))
               (:currency (:balance account))
               (name (:status account))
               (:id account)]})
    account)

  (delete-account [_this account-id]
    (alia/execute
     session
     (str "DELETE FROM accounts WHERE id = ?")
     {:values [account-id]})
    true))

(defn create-account-repository [session]
  (->CassandraAccountRepository session))