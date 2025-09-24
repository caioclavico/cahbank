(ns backend.account.domain.event-test
  (:require [clojure.test :refer :all]
            [backend.account.domain.event :as event]
            [backend.account.domain.model :as model]))

(deftest test-account-opened-event
  (testing "Criação de evento de conta aberta"
    (let [account (model/create-account "12345678901" "João Silva" "joao@email.com")
          evt (event/account-opened-event account)]
      (is (= "account-opened" (:type evt)))
      (is (= (:id account) (:id evt)))
      (is (= (:id account) (get-in evt [:data :account-id])))
      (is (= "12345678901" (get-in evt [:data :document])))
      (is (= "João Silva" (get-in evt [:data :name])))
      (is (= "joao@email.com" (get-in evt [:data :email])))
      (is (= "active" (get-in evt [:data :status])))
      (is (not (nil? (:timestamp evt))))
      (is (= 1 (:version evt))))))

(deftest test-account-updated-event
  (testing "Criação de evento de atualização"
    (let [account (model/create-account "12345678901" "João Silva" "joao@email.com")
          evt (event/account-updated-event account)]
      (is (= "account-updated" (:type evt)))
      (is (not= (:id account) (:id evt))) ; ID do evento é diferente
      (is (= (:id account) (get-in evt [:data :account-id])))
      (is (= "active" (get-in evt [:data :status]))))))

(deftest test-account-closed-event
  (testing "Criação de evento de fechamento"
    (let [account (model/create-account "12345678901" "João Silva" "joao@email.com")
          evt (event/account-closed-event account "Solicitação do cliente")]
      (is (= "account-closed" (:type evt)))
      (is (= (:id account) (get-in evt [:data :account-id])))
      (is (= "Solicitação do cliente" (get-in evt [:data :reason]))))))

(deftest test-account-error-event
  (testing "Criação de evento de erro"
    (let [evt (event/account-error-event "business-error" "12345678901" "account-id" "Já existe uma conta")]
      (is (= "account-error" (:type evt)))
      (is (= "business-error" (get-in evt [:data :error-type])))
      (is (= "12345678901" (get-in evt [:data :document])))
      (is (= "account-id" (get-in evt [:data :account-id])))
      (is (= "Já existe uma conta" (get-in evt [:data :reason]))))))
