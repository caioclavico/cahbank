(ns backend.transaction.infrastructure.persistence.cassandra-repository
  (:require [backend.transaction.application.port.out.transaction-repository :as transaction-repo]
            [qbits.alia :as alia]
            [taoensso.timbre :as log]))

(defrecord CassandraTransactionRepository [session]
  transaction-repo/TransactionRepository

  (save-transaction [_this transaction]
    (try
      (log/info "💾 Saving transaction to Cassandra - ID:" (:id transaction))
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
    (log/info "🔍 Fetching transactions for account:" account-id)
    (try
      ;; Cassandra doesn't support OR natively, so we do two queries
      (let [from-transactions (alia/execute
                                session
                                "SELECT * FROM transactions WHERE from_account_id = ? ALLOW FILTERING"
                                {:values [account-id]})
            to-transactions (alia/execute
                              session
                              "SELECT * FROM transactions WHERE to_account_id = ? ALLOW FILTERING"
                              {:values [account-id]})
            all-transactions (concat from-transactions to-transactions)
            ;; Remove duplicates by ID using a set
            seen-ids (atom #{})
            unique-transactions (filter (fn [tx]
                                          (let [id (:id tx)]
                                            (if (contains? @seen-ids id)
                                              false
                                              (do (swap! seen-ids conj id)
                                                  true))))
                                        all-transactions)]
        unique-transactions)
      (catch Exception e
        (log/error "❌ Error fetching transactions:" (.getMessage e))
        [])))

  (find-by-id [_this transaction-id]
    (log/info "🔍 Fetching transaction by ID:" transaction-id)
    (first
      (alia/execute
        session
        "SELECT * FROM transactions WHERE id = ?"
        {:values [transaction-id]}))))

(defn create-transaction-repository [session]
  (->CassandraTransactionRepository session))