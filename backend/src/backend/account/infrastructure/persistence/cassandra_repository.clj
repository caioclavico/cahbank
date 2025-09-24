(ns backend.account.infrastructure.persistence.cassandra-repository
  (:require [backend.account.application.port.out.account-repository :as account-repo]
            [qbits.alia :as alia]))

(defrecord CassandraAccountRepository [session]
  account-repo/AccountRepository

  (save-account [_this account]
    (try
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
             (:created-at account) "')"))
      account
      (catch Exception e
        (throw (ex-info "Erro ao salvar conta no Cassandra" 
                        {:account-id (:id account) :error (.getMessage e)})))))

  (exists-by-document? [_this document]
    (boolean
      (first
        (alia/execute
          session
          "SELECT id FROM accounts WHERE document = ? LIMIT 1"
          {:values [document]}))))

  (find-by-document [_this document]
    (first
      (alia/execute
        session
        "SELECT * FROM accounts WHERE document = ?"
        {:values [document]})))

  (find-by-id [_this account-id]
    (first
      (alia/execute
        session
        "SELECT * FROM accounts WHERE id = ?"
        {:values [account-id]})))

  (update-account [_this account]
    (alia/execute
      session
      "UPDATE accounts SET name = ?, email = ?, balance_amount = ?, balance_currency = ?, status = ? WHERE id = ?"
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
      "DELETE FROM accounts WHERE id = ?"
      {:values [account-id]})
    true))

(defn create-account-repository [session]
  (->CassandraAccountRepository session))