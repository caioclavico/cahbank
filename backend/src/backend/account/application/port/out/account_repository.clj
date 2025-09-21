(ns backend.account.application.port.out.account-repository)

(defprotocol AccountRepository
  (save-account [this account]
    "Salva uma nova conta")
  (get-account [this account-id]
    "Obtém uma conta pelo ID"))
