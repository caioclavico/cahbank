(ns backend.account.application.service
  (:require [backend.account.application.port.in.account-service :as account-service]
            [backend.account.application.port.out.account-repository :as account-repo]
            [backend.account.application.port.out.event-publisher :as event-pub]
            [backend.account.domain.model :as model]))

(defrecord AccountServiceImpl [repository event-publisher]
  account-service/AccountService
  (open-account [_this document name email]
    (if-not (model/can-open-account? document name email)
      (throw (ex-info "Dados inválidos para abertura de conta"
                      {:type :validation-error}))

      (if (account-repo/exists-by-document? repository document)
        (throw (ex-info "Já existe uma conta para este documento"
                        {:type :business-error}))

        (let [account (model/create-account document name email)]
          (account-repo/save-account repository account)
          (event-pub/publish-account-opened event-publisher account)
          account))))

  (get-account [_this account-id]
    (account-repo/find-by-document repository account-id)
    (if-let [account (account-repo/find-by-id repository account-id)]
      account
      (throw (ex-info "Conta não encontrada" {})))))

(defn create-account-service
  [repository event-publisher]
  (->AccountServiceImpl repository event-publisher))