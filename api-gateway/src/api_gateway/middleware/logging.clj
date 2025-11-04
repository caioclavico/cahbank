(ns api-gateway.middleware.logging
  (:require [taoensso.timbre :as log]))

(defn wrap-logging
  "Middleware para logging de requisições e respostas"
  [handler]
  (fn [request]
    (let [start-time (System/currentTimeMillis)
          method (:request-method request)
          uri (:uri request)]
      
      (log/info "📥 Incoming request:" method uri)
      
      (let [response (handler request)
            duration (- (System/currentTimeMillis) start-time)]
        
        (log/info "📤 Response:" method uri 
                  "| Status:" (:status response)
                  "| Duration:" duration "ms")
        
        response))))
