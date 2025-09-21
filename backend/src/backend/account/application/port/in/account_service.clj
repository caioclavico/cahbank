(ns backend.account.application.port.in.account-service)

(defprotocol AccountService
  (open-account [this document name email]
    "Abre uma nova conta para o cliente")

  (get-account [this account-id]
    "Obtém uma conta pelo ID"))