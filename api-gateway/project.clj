;; Cahbank - API Gateway
(defproject api-gateway "0.1.0-SNAPSHOT"
  :description "API Gateway for Cahbank Digital Bank"
  :url "https://github.com/caioclavico/cahbank"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :min-lein-version "2.9.1"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [com.taoensso/timbre "5.2.1"]
                 [mount "0.1.16"]
                 [compojure "1.7.0"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [ring/ring-json "0.5.1"]
                 [ring-cors "0.1.13"]
                 [clj-http "3.12.3"]
                 [cheshire "6.1.0"]]
  :main ^:skip-aot api-gateway.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.10.9"]]
                   :plugins [[lein-midje "3.2.2"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
