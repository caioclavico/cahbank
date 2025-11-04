(ns backend.transaction.application.service
  (:require [backend.transaction.application.port.in.transaction-service :as transaction-service]
            [backend.transaction.application.port.out.transaction-repository :as transaction-repo]
            [backend.transaction.application.port.out.event-publisher :as event-pub]
            [backend.transaction.domain.model :as model]
            [backend.transaction.domain.event :as event]
            [taoensso.timbre :as log]))

(defrecord TransactionServiceImpl [repository event-publisher]
  transaction-service/TransactionService
  
  (transfer [_this from-account-id to-account-id amount description]
    (try
      (log/info "💸 Processing transfer from" from-account-id "to" to-account-id "amount:" amount)
      
      (if-not (model/can-transfer? from-account-id to-account-id amount)
        (do
          (log/error "❌ Invalid data for transfer")
          (throw (ex-info "Invalid data for transfer"
                          {:type :validation-error})))
        
        (let [transfer (model/create-transfer from-account-id to-account-id amount description)
              transaction (model/->Transaction (:id transfer)
                                        (:from-account-id transfer)
                                        (:to-account-id transfer)
                                        (:amount transfer)
                                        :transfer
                                        :processing
                                        (:description transfer)
                                        (java.time.LocalDateTime/now))]
          
          (transaction-repo/save-transaction repository transaction)
          (event-pub/publish-event event-publisher (event/transaction-created-event transaction))
          (log/info "✅ Transfer created successfully - ID:" (:id transaction))
          transaction))
      (catch Exception e
        (log/error "❌ Error in transfer:" (.getMessage e))
        (throw e))))
  
  (deposit [_this account-id amount description]
    (try
      (log/info "💰 Processing deposit to account" account-id "amount:" amount)
      
      (if-not (model/can-deposit? account-id amount)
        (do
          (log/error "❌ Invalid data for deposit")
          (throw (ex-info "Invalid data for deposit"
                          {:type :validation-error})))
        
        (let [deposit (model/create-deposit account-id amount description)
              transaction (model/->Transaction (:id deposit)
                                        nil
                                        (:account-id deposit)
                                        (:amount deposit)
                                        :deposit
                                        :processing
                                        (:description deposit)
                                        (java.time.LocalDateTime/now))]
          
          (transaction-repo/save-transaction repository transaction)
          (event-pub/publish-event event-publisher (event/transaction-created-event transaction))
          (log/info "✅ Deposit created successfully - ID:" (:id transaction))
          transaction))
      (catch Exception e
        (log/error "❌ Error in deposit:" (.getMessage e))
        (throw e))))
  
  (withdraw [_this account-id amount description]
    (try
      (log/info "💸 Processing withdrawal from account" account-id "amount:" amount)
      
      (if-not (model/can-withdraw? account-id amount)
        (do
          (log/error "❌ Invalid data for withdrawal")
          (throw (ex-info "Invalid data for withdrawal"
                          {:type :validation-error})))
        
        (let [withdrawal (model/create-withdrawal account-id amount description)
              transaction (model/->Transaction (:id withdrawal)
                                        (:account-id withdrawal)
                                        nil
                                        (:amount withdrawal)
                                        :withdrawal
                                        :processing
                                        (:description withdrawal)
                                        (java.time.LocalDateTime/now))]
          
          (transaction-repo/save-transaction repository transaction)
          (event-pub/publish-event event-publisher (event/transaction-created-event transaction))
          (log/info "✅ Withdrawal created successfully - ID:" (:id transaction))
          transaction))
      (catch Exception e
        (log/error "❌ Error in withdrawal:" (.getMessage e))
        (throw e))))
  
  (get-transactions [_this account-id]
    (log/info "📋 Fetching transactions for account:" account-id)
    (transaction-repo/find-by-account repository account-id)))

(defn create-transaction-service
  [repository event-publisher]
  (->TransactionServiceImpl repository event-publisher))
