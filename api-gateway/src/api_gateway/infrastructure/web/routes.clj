(ns api-gateway.infrastructure.web.routes
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]
            [ring.util.response :refer [response status]]
            [api-gateway.gateway.router :as router]))

(defn health-check-handler [_request]
  (-> (response {:status "healthy"
                 :service "api-gateway"
                 :timestamp (str (java.time.Instant/now))})
      (status 200)))

(defn not-found-handler [_request]
  (-> (response {:error "Not Found"
                 :message "The requested resource was not found"})
      (status 404)))

(defroutes app-routes
  ;; Health check
  (GET "/health" [] health-check-handler)
  (GET "/api/health" [] health-check-handler)
  
  ;; Proxy todas as outras requisições para os serviços
  (ANY "*" [] router/route-request)
  
  ;; Catch-all para rotas não encontradas
  (route/not-found not-found-handler))
