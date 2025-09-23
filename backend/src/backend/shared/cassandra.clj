(ns backend.shared.cassandra
  (:require [mount.core :refer [defstate]]
            [qbits.alia :as alia]
            [taoensso.timbre :as log]))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstate cassandra-session
    :start (do
             (log/info "🔌 Connecting to Cassandra...")
             (let [cluster (alia/cluster {:contact-points ["localhost"]
                                          :port 9042})
                   session (alia/connect cluster)]
               (log/info "🔌 Connected to Cassandra!")
               
               ;; Aguardar a conexão estar pronta
               (Thread/sleep 100)
               
               ;; Criar keyspace usando comando SQL direto
               (alia/execute 
                session 
                "CREATE KEYSPACE IF NOT EXISTS digital_bank WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}")
               (log/info "🗄️ Keyspace 'digital_bank' created!")
               
               ;; Usar keyspace
               (alia/execute session "USE digital_bank")
               
               ;; Criar tabela usando comando SQL direto
               (alia/execute 
                session 
                "CREATE TABLE IF NOT EXISTS accounts (
                   id text PRIMARY KEY,
                   document text,
                   name text,
                   email text,
                   balance_amount decimal,
                   balance_currency text,
                   status text,
                   created_at timestamp
                 ) WITH compaction = {'class': 'LeveledCompactionStrategy'}")
               (log/info "📋 Table 'accounts' created!")
               
               ;; Criar índice
               (alia/execute
                session
                "CREATE INDEX IF NOT EXISTS accounts_by_document ON accounts (document)")
               (log/info "🔍 Index 'accounts_by_document' created!")
               
               session))
    :stop (fn [session]
            (log/info " Closing Cassandra connection...")
            (alia/shutdown session)))