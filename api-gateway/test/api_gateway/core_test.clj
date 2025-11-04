(ns api-gateway.core-test
  (:require [clojure.test :refer :all]
            [api-gateway.gateway.router :as router]
            [api-gateway.middleware.rate-limit :as rate-limit]
            [api-gateway.config :as config]))

(deftest routing-test
  (testing "routes /api/accounts to account-service"
    (let [request {:uri "/api/accounts" :request-method :get}
          response (router/route-request request)]
      (is (contains? #{200 503} (:status response)))))
  
  (testing "routes /api/transactions to transaction-service"
    (let [request {:uri "/api/transactions" :request-method :get}
          response (router/route-request request)]
      (is (contains? #{200 503} (:status response)))))
  
  (testing "returns 404 for unknown routes"
    (let [request {:uri "/api/unknown" :request-method :get}
          response (router/route-request request)]
      (is (= 404 (:status response))))))

(deftest rate-limiting-test
  (testing "allows requests within limit"
    (reset! rate-limit/request-counts {})
    (let [handler (fn [_] {:status 200 :body "OK"})
          wrapped (rate-limit/wrap-rate-limit handler)
          request {:remote-addr "127.0.0.1"}
          response (wrapped request)]
      (is (= 200 (:status response))))))

(deftest config-test
  (testing "can read config values"
    (is (= 8080 (config/get-config :server :port)))
    (is (= "0.0.0.0" (config/get-config :server :host)))
    (is (= true (config/get-config :rate-limit :enabled)))))
