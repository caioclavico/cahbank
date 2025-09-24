(ns backend.account.application.service-test
  (:require [clojure.test :refer :all]
            [backend.account.application.service :as service]
            [backend.account.domain.model :as model]
            [backend.account.application.port.out.account-repository :as account-repo]
            [backend.account.application.port.out.event-publisher :as event-pub]
            [backend.account.application.port.in.account-service :as account-service]))

;; Mock implementations for testing
(defrecord MockRepository [accounts]
  account-repo/AccountRepository
  (save-account [_this account] account)
  (exists-by-document? [_this document] 
    (boolean (some #(= (:document %) document) @accounts)))
  (find-by-document [_this document]
    (first (filter #(= (:document %) document) @accounts)))
  (find-by-id [_this account-id]
    (first (filter #(= (:id %) account-id) @accounts)))
  (update-account [_this account]
    (swap! accounts (fn [accs] (map #(if (= (:id %) (:id account)) account %) accs)))
    account)
  (delete-account [_this account-id]
    (swap! accounts (fn [accs] (remove #(= (:id %) account-id) accs)))
    true))

(defrecord MockEventPublisher [events]
  event-pub/EventPublisher
  (publish-event [_this event]
    (swap! events conj event)))

(defn create-test-service
  ([] (create-test-service (atom []) (atom [])))
  ([accounts events]
   (service/create-account-service 
     (->MockRepository accounts)
     (->MockEventPublisher events))))

(deftest test-open-account-success
  (testing "Abertura de conta com sucesso"
    (let [accounts (atom [])
          events (atom [])
          service-instance (create-test-service accounts events)
          result (account-service/open-account service-instance "12345678901" "João Silva" "joao@email.com")]
      (is (= "12345678901" (:document result)))
      (is (= "João Silva" (:name result)))
      (is (= "joao@email.com" (:email result)))
      (is (= :active (:status result)))
      (is (= 1 (count @events)))
      (is (= "account-opened" (:type (first @events)))))))

(deftest test-open-account-duplicate
  (testing "Tentativa de abertura de conta duplicada"
    (let [existing-account (model/create-account "12345678901" "João Silva" "joao@email.com")
          accounts (atom [existing-account])
          events (atom [])
          service-instance (create-test-service accounts events)]
      (is (thrown? Exception (account-service/open-account service-instance "12345678901" "Maria Santos" "maria@email.com")))
      (is (= 1 (count @events)))
      (is (= "account-error" (:type (first @events))))
      (is (= "business-error" (get-in (first @events) [:data :error-type]))))))

(deftest test-open-account-invalid-data
  (testing "Abertura de conta com dados inválidos"
    (let [accounts (atom [])
          events (atom [])
          service-instance (create-test-service accounts events)]
      (is (thrown? Exception (account-service/open-account service-instance "123" "João Silva" "joao@email.com")))
      (is (= 1 (count @events)))
      (is (= "account-error" (:type (first @events))))
      (is (= "validation-error" (get-in (first @events) [:data :error-type]))))))

(deftest test-update-account-success
  (testing "Atualização de conta com sucesso"
    (let [existing-account (model/create-account "12345678901" "João Silva" "joao@email.com")
          accounts (atom [existing-account])
          events (atom [])
          service-instance (create-test-service accounts events)
          result (account-service/update-account service-instance (:id existing-account) "João Santos" "joao.santos@email.com")]
      (is (= "João Santos" (:name result)))
      (is (= "joao.santos@email.com" (:email result)))
      (is (= 1 (count @events)))
      (is (= "account-updated" (:type (first @events)))))))

(deftest test-update-account-not-found
  (testing "Atualização de conta não encontrada"
    (let [accounts (atom [])
          events (atom [])
          service-instance (create-test-service accounts events)]
      (is (thrown? Exception (account-service/update-account service-instance "non-existent-id" "João Santos" "joao@email.com")))
      (is (= 1 (count @events)))
      (is (= "account-error" (:type (first @events))))
      (is (= "not-found" (get-in (first @events) [:data :error-type]))))))

(deftest test-close-account-success
  (testing "Fechamento de conta com sucesso"
    (let [existing-account (model/create-account "12345678901" "João Silva" "joao@email.com")
          accounts (atom [existing-account])
          events (atom [])
          service-instance (create-test-service accounts events)
          result (account-service/close-account service-instance (:id existing-account) "Solicitação do cliente")]
      (is (= :closed (:status result)))
      (is (= 1 (count @events)))
      (is (= "account-closed" (:type (first @events)))))))
