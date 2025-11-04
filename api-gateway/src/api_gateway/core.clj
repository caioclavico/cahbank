(ns api-gateway.core
  (:require [taoensso.timbre :as log]
            [mount.core :as mount]
            [api-gateway.infrastructure.web.server])
  (:gen-class))

(defn start-gateway []
  (log/info "🚀 Starting API Gateway...")
  (mount/start)
  (log/info "✅ API Gateway started successfully!"))

(defn -main [& _args]
  (start-gateway)
  
  ;; Register shutdown hook for graceful shutdown
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(do
                                (log/info "🛑 Received shutdown signal...")
                                (mount/stop))))
  
  (log/info "📌 API Gateway running on port 8080")
  (log/info "📌 Leiningen: Ctrl+C | Docker: docker-compose down")
  
  ;; Keep process alive
  @(promise))
