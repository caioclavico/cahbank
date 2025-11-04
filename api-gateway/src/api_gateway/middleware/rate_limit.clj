(ns api-gateway.middleware.rate-limit
  (:require [taoensso.timbre :as log]
            [api-gateway.config :as config]))

(def request-counts (atom {}))

(defn- get-client-id [request]
  ;; Usa IP como identificador do cliente
  (or (get-in request [:headers "x-forwarded-for"])
      (:remote-addr request)
      "unknown"))

(defn- cleanup-old-entries []
  ;; Remove entradas antigas do cache
  (let [now (System/currentTimeMillis)
        window-ms (config/get-config :rate-limit :window-ms)]
    (swap! request-counts 
           (fn [counts]
             (into {} 
                   (filter (fn [[_ v]] 
                             (> (+ (:timestamp v) window-ms) now))
                           counts))))))

(defn- check-rate-limit [client-id]
  (let [now (System/currentTimeMillis)
        window-ms (config/get-config :rate-limit :window-ms)
        max-requests (config/get-config :rate-limit :max-requests)
        client-data (get @request-counts client-id)]
    
    (if (and client-data (> (+ (:timestamp client-data) window-ms) now))
      ;; Dentro da janela de tempo
      (if (< (:count client-data) max-requests)
        ;; Permite a requisição
        (do
          (swap! request-counts update client-id 
                 (fn [data] {:count (inc (:count data))
                             :timestamp (:timestamp data)}))
          true)
        ;; Limite excedido
        false)
      ;; Nova janela de tempo ou primeira requisição
      (do
        (swap! request-counts assoc client-id {:count 1 :timestamp now})
        true))))

(defn wrap-rate-limit
  "Middleware para rate limiting"
  [handler]
  (fn [request]
    (if (config/get-config :rate-limit :enabled)
      (let [client-id (get-client-id request)]
        (cleanup-old-entries)
        
        (if (check-rate-limit client-id)
          (handler request)
          (do
            (log/warn "⚠️  Rate limit exceeded for client:" client-id)
            {:status 429
             :headers {"Content-Type" "application/json"
                       "Retry-After" "60"}
             :body {:error "Too Many Requests"
                    :message "Rate limit exceeded. Please try again later."}})))
      (handler request))))
