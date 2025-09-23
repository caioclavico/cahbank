(ns backend.account.application.service
  (:require [backend.account.application.port.in.account-service :as account-service]
            [backend.account.application.port.out.account-repository :as account-repo]
            [backend.account.application.port.out.event-publisher :as event-pub]
            [backend.account.domain.model :as model]
            [backend.account.domain.event :as event]))

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
          (event-pub/publish-event event-publisher (event/account-opened-event account))
          account))))

  (update-account [_this account-id name email]
    (if-let [account (account-repo/find-by-id repository account-id)]
      (if (model/can-update-account? account name email)
        (let [updated-account (model/update-account-data account name email)]
          (account-repo/update-account repository updated-account)
          (event-pub/publish-event event-publisher 
            (event/account-updated-event updated-account))
          updated-account)
        (throw (ex-info "Dados inválidos para atualização" 
                        {:type :validation-error})))
      (throw (ex-info "Conta não encontrada" 
                      {:type :not-found}))))

  (close-account [_this account-id reason]
    (if-let [account (account-repo/find-by-id repository account-id)]
      (if (= (:status account) :closed)
        (throw (ex-info "Conta já está fechada" 
                        {:type :business-error}))
        (let [closed-account (model/close-account account reason)]
          (account-repo/update-account repository closed-account)
          (event-pub/publish-event event-publisher 
            (event/account-closed-event closed-account reason))
          closed-account))
      (throw (ex-info "Conta não encontrada" 
                      {:type :not-found})))))

(defn create-account-service
  [repository event-publisher]
  (->AccountServiceImpl repository event-publisher))