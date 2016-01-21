(ns wbk-chat-be.websockets
  (:require [cognitect.transit :as t]))

(defonce ws-chan (atom nil))

(def json-reader (t/reader :json))

(defn receive-transit-msg! [update-fn]
  (fn [msg] (update-fn (->> msg .-data (t/read json-reader)))))

(defn send-transit-msg! [msg]
  (if @ws-chan
    (.send @ws-chan msg #_(t/write json-writer msg))
    (throw (js/Error. "Websocket is not available!"))))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect websocket")
  (if-let [chan (js/WebSocket. url)]
    (do (set! (.-onmessage chan) (receive-transit-msg! receive-handler))
        (reset! ws-chan chan)
        (println "Websocket connection established with: " url))
    (throw (js/Error. "Websocket connection failed!"))))
