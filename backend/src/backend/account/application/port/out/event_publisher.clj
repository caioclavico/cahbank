(ns backend.account.application.port.out.event-publisher)

(defprotocol EventPublisher
  (publish-event [this event]
    "Publica um evento"))
