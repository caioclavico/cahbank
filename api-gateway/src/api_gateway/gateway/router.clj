(ns api-gateway.gateway.router
  (:require [api-gateway.gateway.proxy :as proxy]))

(def route-mappings
  {:account-service {:prefix "/api/accounts"
                     :routes #{"/api/accounts"
                               "/api/accounts/:id"}}
   
   :transaction-service {:prefix "/api/transactions"
                         :routes #{"/api/transactions"
                                   "/api/transactions/:id"}}})

(defn- match-service [path]
  (cond
    (re-matches #"/api/accounts.*" path) :account-service
    (re-matches #"/api/transactions.*" path) :transaction-service
    :else nil))

(defn route-request
  "Route incoming request to the appropriate service"
  [request]
  (let [path (:uri request)
        service (match-service path)]
    
    (if service
      (proxy/forward-request service path request)
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body {:error "Route not found"
              :path path}})))
