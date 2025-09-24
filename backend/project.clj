;; Cahbank - Digital Bank Backend Core Services
(defproject backend "0.1.0-SNAPSHOT"
  :description "Digital Bank Backend Core Services"
  :url "https://github.com/caioclavico/cahbank"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :min-lein-version "2.9.1"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.clojars.caioclavico/kafka-metamorphosis "0.4.0"]
                 [com.taoensso/timbre "5.2.1"]
                 [cc.qbits/alia-all "4.3.7-beta1"]
                 [cc.qbits/hayt "4.1.0"]
                 [cheshire "6.1.0"]
                 [mount "0.1.16"]
                 [compojure "1.7.0"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [ring/ring-json "0.5.1"]
                 [ring-cors "0.1.13"]]
  :main ^:skip-aot backend.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.10.9"]]       ; Testes
                   :plugins [[lein-midje "3.2.2"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
