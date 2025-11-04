(ns api-gateway.infrastructure.web.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [api-gateway.infrastructure.web.routes :refer [app-routes]]
            [api-gateway.middleware.auth :as auth]
            [api-gateway.middleware.logging :as logging]
            [api-gateway.middleware.rate-limit :as rate-limit]
            [api-gateway.config :as config]
            [taoensso.timbre :as log]
            [mount.core :refer [defstate]]))

(defn wrap-middlewares [handler]
  (-> handler
      auth/wrap-authentication
      rate-limit/wrap-rate-limit
      wrap-params
      (wrap-json-body {:keywords? true})
      wrap-json-response
      logging/wrap-logging
      (wrap-cors :access-control-allow-origin (config/get-config :cors :allowed-origins)
                 :access-control-allow-methods (config/get-config :cors :allowed-methods)
                 :access-control-allow-headers (config/get-config :cors :allowed-headers)
                 :access-control-expose-headers (config/get-config :cors :exposed-headers)
                 :access-control-allow-credentials (config/get-config :cors :allow-credentials))))

(def app
  (wrap-middlewares app-routes))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate server
  :start (let [port (config/get-config :server :port)
               host (config/get-config :server :host)]
           (log/info "�� Starting API Gateway server on" (str host ":" port))
           (jetty/run-jetty app {:port port
                                 :host host
                                 :join? false}))
  :stop (when server
          (log/info "⏹️  Stopping API Gateway server")
          (.stop server)))
