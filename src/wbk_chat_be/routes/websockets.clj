(ns wbk-chat-be.routes.websockets
  (:require [compojure.core :refer [GET defroutes]]
            [clojure.tools.logging :as log]
            [immutant.web.async :as async]))

(defonce channels (atom []))

#_(defn ws-handler [request]
  (let [id (:id (:params request))]
    (letfn [(connect! [channel]
              (println "connected: " id)
              (swap! channels conj {:id id :channel channel}))
            (disconnect! [channel {:keys [code reason]}]
              (println "disconnected: " id)
              (swap! channels #(remove (fn [x] (= id (:id x))) %)))
            (send-msg! [channel msg]
              (println "@@@@@@@@@@" msg))]
      (async/as-channel request {:on-open connect!
                                 :on-close disconnect!
                                 :on-message send-msg!}))))

(defn connect! [channel]
  (log/info "channel open")
  (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
  (log/info "close code:" code "reason:" reason)
  (swap! channels #(remove #{channel} %)))

(defn notify-clients! [channel msg]
  (doseq [channel @channels]
    (async/send! channel msg)))

(def websocket-callbacks
  "WebSocket callback functions"
  {:on-open connect!
   :on-close disconnect!
   :on-message notify-clients!})

(defn ws-handler [request]
  (async/as-channel request websocket-callbacks))

(defroutes websocket-routes
  (GET "/ws/:id" [] ws-handler))
