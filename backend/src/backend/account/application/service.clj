(ns backend.account.application.service
  (:require [backend.account.application.port.in.account-service :as account-service]
            [backend.account.application.port.out.account-repository :as account-repo]
            [backend.account.application.port.out.event-publisher :as event-pub]
            [backend.account.domain.model :as model]
            [backend.account.domain.event :as event]
            [taoensso.timbre :as log]))

(defrecord AccountServiceImpl [repository event-publisher]
  account-service/AccountService
  
  (open-account [_this document name email]
    (try
      (log/info "Abrindo conta para:" name "| Documento:" document)
      
      (if-not (model/can-open-account? document name email)
        (do
          (log/error "Dados inválidos para abertura de conta")
          (event-pub/publish-event event-publisher 
            (event/account-error-event "validation-error" document nil 
              "Dados inválidos para abertura de conta"))
          (throw (ex-info "Dados inválidos para abertura de conta"
                          {:type :validation-error})))

        (if (account-repo/exists-by-document? repository document)
          (do
            (log/error "Já existe uma conta para este documento")
            (event-pub/publish-event event-publisher 
              (event/account-error-event "business-error" document nil 
                "Já existe uma conta para este documento"))
            (throw (ex-info "Já existe uma conta para este documento"
                            {:type :business-error})))

          (let [account (model/create-account document name email)]
            (account-repo/save-account repository account)
            (event-pub/publish-event event-publisher (event/account-opened-event account))
            (log/info "Conta criada com sucesso - ID:" (:id account))
            account)))
      (catch Exception e
        (log/error "Erro na abertura de conta:" (.getMessage e))
        (throw e))))

  (update-account [_this account-id name email]
    (log/info "Atualizando conta:" account-id)
    (if-let [account (account-repo/find-by-id repository account-id)]
      (if (model/can-update-account? account name email)
        (let [updated-account (model/update-account-data account name email)]
          (account-repo/update-account repository updated-account)
          (event-pub/publish-event event-publisher 
            (event/account-updated-event updated-account))
          (log/info "Conta atualizada com sucesso - ID:" (:id updated-account))
          updated-account)
        (do
          (event-pub/publish-event event-publisher 
            (event/account-error-event "validation-error" (:document account) account-id 
              "Dados inválidos para atualização"))
          (throw (ex-info "Dados inválidos para atualização" 
                          {:type :validation-error}))))
      (do
        (event-pub/publish-event event-publisher 
          (event/account-error-event "not-found" nil account-id 
            "Conta não encontrada"))
        (throw (ex-info "Conta não encontrada" 
                        {:type :not-found})))))

  (close-account [_this account-id reason]
    (log/info "Fechando conta:" account-id "| Motivo:" reason)
    (if-let [account (account-repo/find-by-id repository account-id)]
      (if (= (:status account) :closed)
        (do
          (event-pub/publish-event event-publisher 
            (event/account-error-event "business-error" (:document account) account-id 
              "Conta já está fechada"))
          (throw (ex-info "Conta já está fechada" 
                          {:type :business-error})))
        (let [closed-account (model/close-account account reason)]
          (account-repo/update-account repository closed-account)
          (event-pub/publish-event event-publisher 
            (event/account-closed-event closed-account reason))
          (log/info "Conta fechada com sucesso - ID:" (:id closed-account))
          closed-account))
      (do
        (event-pub/publish-event event-publisher 
          (event/account-error-event "not-found" nil account-id 
            "Conta não encontrada"))
        (throw (ex-info "Conta não encontrada" 
                        {:type :not-found}))))))

(defn create-account-service
  [repository event-publisher]
  (->AccountServiceImpl repository event-publisher))