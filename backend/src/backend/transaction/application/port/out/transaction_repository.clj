(ns backend.transaction.application.port.out.transaction-repository)

(defprotocol TransactionRepository
  (save-transaction [this transaction]
    "Salva uma transação")
  
  (find-by-account [this account-id]
    "Busca transações de uma conta")
  
  (find-by-id [this transaction-id]
    "Busca transação por ID"))
