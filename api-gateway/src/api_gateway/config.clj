(ns api-gateway.config)

(def config
  {:server {:port 8080
            :host "0.0.0.0"}
   
   :services {:account-service {:base-url "http://localhost:8081"
                                 :timeout 5000}
              :transaction-service {:base-url "http://localhost:8082"
                                    :timeout 5000}}
   
   :cors {:allowed-origins ["http://localhost:3000" "http://localhost:8080"]
          :allowed-methods [:get :post :put :delete :options]
          :allowed-headers ["Content-Type" "Authorization"]
          :exposed-headers ["Content-Type"]
          :allow-credentials true}
   
   :rate-limit {:enabled true
                :max-requests 100
                :window-ms 60000}})

(defn get-config [& path]
  (get-in config path))
