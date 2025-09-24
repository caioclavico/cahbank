(ns backend.transaction.application.port.in.transaction-service)

(defprotocol TransactionService
  (transfer [this from-account-id to-account-id amount description]
    "Transfere valor entre contas")
  
  (deposit [this account-id amount description]
    "Deposita valor em uma conta")
  
  (withdraw [this account-id amount description]
    "Saca valor de uma conta")
  
  (get-transactions [this account-id]
    "Busca transações de uma conta"))
