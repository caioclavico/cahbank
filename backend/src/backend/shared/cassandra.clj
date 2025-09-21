(ns backend.shared.cassandra
  (:require [mount.core :refer [defstate]]
            [qbits.alia :as alia]
            [qbits.hayt :refer [create-keyspace create-table create-index
                                if-exists if-not-exists column-definitions
                                with column and on-table]]
            [taoensso.timbre :as log]))

(def cassandra-config
  {:contact-points ["localhost"]
   :port 9042
   :keyspace "digital_bank"})


#_(defstate cassandra-session
    :start (do
             (log/info "🔌 Connecting to Cassandra...")
             (alia/connect cassandra-config))
    :stop (fn [session]
            (log/info "🔌 Closing Cassandra connection...")
            (alia/shutdown session)))

#_(defn init-keyspace []
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (alia/execute
     cassandra-session
     (create-keyspace :digital_bank
                      (if-exists false)
                      (with {:replication {:class "SimpleStrategy"
                                           :replication_factor 1}}))))

#_(defn init-tables []
    (alia/execute
     cassandra-session
     (create-table :accounts
                   (if-not-exists true)
                   (column-definitions {:id :text
                                        :document :text
                                        :name :text
                                        :email :text
                                        :balance_amount :decimal
                                        :balance_currency :text
                                        :status :text
                                        :created_at :timestamp
                                        :primary-key [:id]})
                   (with {:compaction {:class "LeveledCompactionStrategy"}})))

    (alia/execute
     cassandra-session
     (create-index :accounts_by_document
                   (if-not-exists true)
                   (on-table :accounts)
                   (and (column :document)))))