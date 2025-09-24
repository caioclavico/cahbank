(ns backend.transaction.infrastructure.persistence.cassandra-repository
  (:require [backend.transaction.application.port.out.transaction-repository :as transaction-repo]
            [qbits.alia :as alia]
            [taoensso.timbre :as log]))

(defrecord CassandraTransactionRepository [session]
  transaction-repo/TransactionRepository

  (save-transaction [_this transaction]
    (try
      (log/info "💾 Salvando transação no Cassandra - ID:" (:id transaction))
      (alia/execute
        session
        (str "INSERT INTO transactions (id, from_account_id, to_account_id, amount, type, status, description, timestamp) "
             "VALUES ('" (:id transaction) "', "
             (if (:from-account-id transaction) (str "'" (:from-account-id transaction) "'") "null") ", "
             (if (:to-account-id transaction) (str "'" (:to-account-id transaction) "'") "null") ", "
             (:amount transaction) ", '"
             (name (:type transaction)) "', '"
             (name (:status transaction)) "', '"
             (:description transaction) "', '"
             (:timestamp transaction) "')"))
      (log/info "✅ Transação salva com sucesso")
      transaction
      (catch Exception e
        (log/error "❌ Erro ao salvar transação:" (.getMessage e))
        (throw (ex-info "Erro ao salvar transação no Cassandra" 
                        {:transaction-id (:id transaction) :error (.getMessage e)})))))

  (find-by-account [_this account-id]
    (log/info "�� Buscando transações da conta:" account-id)
    (alia/execute
      session
      "SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY timestamp DESC"
      {:values [account-id account-id]}))

  (find-by-id [_this transaction-id]
    (log/info "�� Buscando transação por ID:" transaction-id)
    (first
      (alia/execute
        session
        "SELECT * FROM transactions WHERE id = ?"
        {:values [transaction-id]}))))

(defn create-transaction-repository [session]
  (->CassandraTransactionRepository session))