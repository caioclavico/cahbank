(ns backend.account.application.port.in.account-service)

(defprotocol AccountService
  (open-account [this document name email]
    "Abre uma nova conta para o cliente")
  
  (update-account [this account-id name email]
    "Atualiza dados da conta (nome e email)")
  
  (close-account [this account-id reason]
    "Fecha uma conta"))