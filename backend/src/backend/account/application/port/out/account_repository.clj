(ns backend.account.application.port.out.account-repository)

(defprotocol AccountRepository
  (save-account [this account]
    "Salva uma nova conta")
  (exists-by-document? [this document]
    "Verifica se existe uma conta com o documento informado")
  (find-by-document [this document]
    "Busca uma conta pelo documento")
  (find-by-id [this account-id]
    "Busca uma conta pelo ID")
  (update-account [this account]
    "Atualiza uma conta existente")
  (delete-account [this account-id]
    "Remove uma conta"))
