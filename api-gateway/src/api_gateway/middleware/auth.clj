(ns api-gateway.middleware.auth
  (:require [taoensso.timbre :as log]))

(defn- validate-token [token]
  ;; TODO: Implementar validação real de JWT/token
  ;; Por enquanto, aceita qualquer token não vazio
  (and token (seq token)))

(defn- extract-token [request]
  (when-let [auth-header (get-in request [:headers "authorization"])]
    (when (re-matches #"Bearer .+" auth-header)
      (subs auth-header 7))))

(defn wrap-authentication
  "Middleware para autenticação de requisições"
  [handler]
  (fn [request]
    (let [path (:uri request)
          public-paths #{"/health" "/api/health"}]
      
      ;; Permite acesso a rotas públicas
      (if (contains? public-paths path)
        (handler request)
        
        ;; Valida token para rotas protegidas
        (if-let [token (extract-token request)]
          (if (validate-token token)
            (do
              (log/debug "✅ Authentication successful for" path)
              (handler (assoc request :user-token token)))
            (do
              (log/warn "⚠️  Invalid token for" path)
              {:status 401
               :headers {"Content-Type" "application/json"}
               :body {:error "Unauthorized"
                      :message "Invalid authentication token"}}))
          (do
            (log/warn "⚠️  No token provided for" path)
            {:status 401
             :headers {"Content-Type" "application/json"}
             :body {:error "Unauthorized"
                    :message "Authentication token required"}}))))))
