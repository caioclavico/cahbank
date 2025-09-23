(ns backend.account.infrastructure.persistence.in-memory-repository
  (:require [backend.account.application.port.out.account-repository :as account-repo]))

(defrecord InMemoryAccountRepository [accounts]
  account-repo/AccountRepository

  (save-account [this account]
    (swap! (:accounts this) assoc (:id account) account)
    account)

  (get-account [this account-id]
    (get @(:accounts this) account-id))

  (exists-by-document? [this document]
    (boolean
     (some #(= (:document %) document)
           (vals @(:accounts this)))))

  (find-by-document [this document]
    (first
     (filter #(= (:document %) document)
             (vals @(:accounts this)))))

  (find-by-id [this account-id]
    (get @(:accounts this) account-id)))

(defn create-in-memory-account-repository
  "Cria um repositório em memória para testes"
  []
  (->InMemoryAccountRepository (atom {})))

(defn create-in-memory-account-repository-with-data
  "Cria um repositório em memória com dados iniciais para testes"
  [initial-accounts]
  (->InMemoryAccountRepository (atom (into {} (map #(vector (:id %) %) initial-accounts)))))

(defn clear-repository!
  "Limpa todos os dados do repositório em memória (útil para testes)"
  [repository]
  (reset! (:accounts repository) {}))
