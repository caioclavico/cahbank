(ns backend.shared.kafka-config)

(def kafka-bootstrap-servers
  (or (System/getenv "KAFKA_BOOTSTRAP_SERVERS") "localhost:9092"))

(defn consumer-config [group-id]
  {:bootstrap-servers kafka-bootstrap-servers
   :group-id group-id
   :key-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :value-deserializer "org.apache.kafka.common.serialization.StringDeserializer"
   :auto-offset-reset "earliest"})

(defn producer-config []
  {:bootstrap-servers kafka-bootstrap-servers
   :key-serializer "org.apache.kafka.common.serialization.StringSerializer"
   :value-serializer "org.apache.kafka.common.serialization.StringSerializer"})
