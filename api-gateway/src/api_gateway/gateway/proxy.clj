(ns api-gateway.gateway.proxy
  (:require [clj-http.client :as http]
            [taoensso.timbre :as log]
            [api-gateway.config :as config]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn- build-url [service-name path]
  (let [base-url (config/get-config :services service-name :base-url)
        ;; Remove o prefixo /api do path antes de encaminhar
        clean-path (str/replace path #"^/api" "")]
    (str base-url clean-path)))

(defn- get-timeout [service-name]
  (config/get-config :services service-name :timeout))

(defn- extract-headers [request]
  (select-keys (:headers request) 
               ["authorization" "content-type" "accept"]))

(defn- handle-response [response]
  {:status (:status response)
   :headers {"Content-Type" "application/json"}
   ;; O body já vem parseado como mapa pelo clj-http (:as :json)
   ;; Deixamos como mapa para o wrap-json-response serializar
   :body (:body response)})

(defn- handle-error [e service-name]
  (log/error "❌ Proxy Error for" service-name ":" (.getMessage e))
  {:status 503
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string 
          {:error "Service Unavailable"
           :message (str "Failed to reach " service-name)
           :details (.getMessage e)})})

(defn forward-request
  "Forward HTTP request to the specified service"
  [service-name path request]
  (try
    (let [url (build-url service-name path)
          timeout (get-timeout service-name)
          headers (extract-headers request)
          method (or (:request-method request) :get)
          ;; O body já vem parseado pelo wrap-json-body no middleware
          ;; Se existir, será um mapa Clojure que precisamos serializar
          body (let [b (:body request)]
                 (cond
                   (nil? b) nil
                   (string? b) b
                   (map? b) (json/generate-string b)
                   ;; Ignorar InputStreams e outros tipos
                   :else (do
                           (log/warn "⚠️  Unexpected body type:" (type b))
                           nil)))]
      
      (log/info "🔀 Proxying" method "request to" url "| Body:" (if body "present" "none"))
      (log/debug "Request details:" {:method method :url url :has-body (some? body)})
      
      (let [request-opts (cond-> {:method method
                                   :url url
                                   :headers headers
                                   :query-params (:query-params request)
                                   :socket-timeout timeout
                                   :conn-timeout timeout
                                   :throw-exceptions false
                                   :as :json}
                           body (assoc :body body))]
        
        (log/debug "Making HTTP request with opts keys:" (keys request-opts))
        (let [response (http/request request-opts)]
          (log/info "✅ Received response from" service-name "- Status:" (:status response))
          (handle-response response))))
    
    (catch Exception e
      (handle-error e service-name))))
