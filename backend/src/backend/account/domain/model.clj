(ns backend.account.domain.model
  (:import [java.util UUID]))

(defrecord Account [id document name email balance status created-at])

(defn create-account
  "Cria uma nova conta com saldo zero"
  [document name email]
  (->Account (str (UUID/randomUUID))
             document
             name
             email
             {:amount 0M :currency "BRL"}
             :active
             (java.time.LocalDateTime/now)))

(defn update-account-data
  "Atualiza dados da conta (nome e email)"
  [account name email]
  (-> account
      (assoc :name name)
      (assoc :email email)))

(defn close-account
  "Fecha uma conta"
  [account _reason]
  (assoc account :status :closed))

(defn valid-document?
  "Valida CPF (11 dígitos)"
  [document]
  (and (string? document)
       (= (count document) 11)
       (every? #(Character/isDigit %) document)))

(defn valid-email?
  "Valida email básico"
  [email]
  (and (string? email)
       (re-matches #".+@.+\..+" email)))

(defn can-open-account?
  "Valida dados para abrir conta"
  [document name email]
  (and (valid-document? document)
       (valid-email? email)
       (string? name)
       (>= (count name) 2)))

(defn can-update-account?
  "Valida se pode atualizar conta"
  [account name email]
  (and (not= (:status account) :closed)
       (valid-email? email)
       (string? name)
       (>= (count name) 2)))