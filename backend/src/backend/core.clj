(ns backend.core
  (:require [taoensso.timbre :as log]
            [mount.core :as mount])
  (:gen-class))

(defn start-backend []
  (mount/start)
  (log/info "Backend services started successfully!"))

(defn stop-backend []
  (mount/stop)
  (log/info "Backend services stopped."))

(defn -main [& _args]
  (start-backend))