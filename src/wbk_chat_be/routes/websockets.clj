(ns wbk-chat-be.routes.websockets
  (:require [compojure.core :refer [GET defroutes]]
            [org.httpkit.server :refer [send! with-channel on-close on-receive]]
            [clojure.data.json :as json]))

(defonce channels (atom {}))

(defn send-msg [to msg]
  (when-let [ch (first (filter #(= to (first (vals %))) @channels))]
    (send! (first ch) (json/write-str msg))))

(defn notify-clients [message]
  (let [{:keys [token to msg]} (json/read-str message :key-fn keyword)
        ch (first (filter #(= to (second %)) @channels))]
    (send! (first ch) (json/write-str msg))))

(defn connect! [channel id]
  (swap! channels assoc channel id))

(defn disconnect! [channel status]
  (swap! channels dissoc channel))

(defn ws-handler [request]
  (let [id (Integer/parseInt (:id (:params request)))]
    (with-channel request channel
      (connect! channel id)
      (on-close channel (partial disconnect! channel))
      (on-receive channel #(notify-clients %)))))

(defroutes websocket-routes
  (GET "/ws/:id" request (ws-handler request)))
