(ns backend.account.domain.model-test
  (:require [clojure.test :refer :all]
            [backend.account.domain.model :as model]))

(deftest test-create-account
  (testing "Criação de conta válida"
    (let [account (model/create-account "12345678901" "João Silva" "joao@email.com")]
      (is (not (nil? (:id account))))
      (is (= "12345678901" (:document account)))
      (is (= "João Silva" (:name account)))
      (is (= "joao@email.com" (:email account)))
      (is (= {:amount 0M :currency "BRL"} (:balance account)))
      (is (= :active (:status account)))
      (is (not (nil? (:created-at account)))))))

(deftest test-update-account-data
  (testing "Atualização de dados da conta"
    (let [account (model/create-account "12345678901" "João Silva" "joao@email.com")
          updated (model/update-account-data account "João Santos" "joao.santos@email.com")]
      (is (= "João Santos" (:name updated)))
      (is (= "joao.santos@email.com" (:email updated)))
      (is (= (:id account) (:id updated)))
      (is (= (:document account) (:document updated))))))

(deftest test-close-account
  (testing "Fechamento de conta"
    (let [account (model/create-account "12345678901" "João Silva" "joao@email.com")
          closed (model/close-account account "Solicitação do cliente")]
      (is (= :closed (:status closed)))
      (is (= (:id account) (:id closed))))))

(deftest test-valid-document
  (testing "Validação de documento"
    (is (true? (model/valid-document? "12345678901")))
    (is (false? (model/valid-document? "1234567890")))  ; 10 dígitos
    (is (false? (model/valid-document? "123456789012"))) ; 12 dígitos
    (is (false? (model/valid-document? "1234567890a")))  ; com letra
    (is (false? (model/valid-document? nil)))))

(deftest test-valid-email
  (testing "Validação de email"
    (is (true? (model/valid-email? "joao@email.com")))
    (is (true? (model/valid-email? "test@domain.org")))
    (is (false? (model/valid-email? "email-sem-arroba")))
    (is (false? (model/valid-email? "@domain.com")))
    (is (false? (model/valid-email? "user@")))
    (is (false? (model/valid-email? nil)))))

(deftest test-can-open-account
  (testing "Validação para abertura de conta"
    (is (true? (model/can-open-account? "12345678901" "João Silva" "joao@email.com")))
    (is (false? (model/can-open-account? "1234567890" "João Silva" "joao@email.com"))) ; doc inválido
    (is (false? (model/can-open-account? "12345678901" "J" "joao@email.com"))) ; nome muito curto
    (is (false? (model/can-open-account? "12345678901" "João Silva" "email-invalido"))))) ; email inválido

(deftest test-can-update-account
  (testing "Validação para atualização de conta"
    (let [active-account (assoc (model/create-account "12345678901" "João Silva" "joao@email.com") :status :active)
          closed-account (assoc (model/create-account "12345678901" "João Silva" "joao@email.com") :status :closed)]
      (is (true? (model/can-update-account? active-account "João Santos" "joao@email.com")))
      (is (false? (model/can-update-account? closed-account "João Santos" "joao@email.com"))) ; conta fechada
      (is (false? (model/can-update-account? active-account "J" "joao@email.com"))) ; nome muito curto
      (is (false? (model/can-update-account? active-account "João Santos" "email-invalido")))))) ; email inválido
