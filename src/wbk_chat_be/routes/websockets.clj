(ns wbk-chat-be.routes.websockets
  (:require [wbk-chat-be.db.users :as du]
            [wbk-chat-be.db.messages :as dm]
            [wbk-chat-be.dates :as d]
            [compojure.core :refer [GET defroutes]]
            [org.httpkit.server :refer [send! with-channel on-close on-receive]]
            [clojure.data.json :as json]))

(defonce channels (atom {}))

(defn send-msg [message]
  (let [{:keys [token to msg]} (json/read-str message :key-fn keyword)
        from (du/find-user-by-token token)]
    (when from
      (when-let [rslt (dm/send-msg (:id from) to msg)]
        (du/update-last-msg-seen (:id from) (:id (last rslt)))
        (let [dt (d/unparse-datetime (d/sql->date (:datetime rslt)))
              ch (first (first (filter #(= to (second %)) @channels)))]
          (when (and ch
                     (send! ch
                            (json/write-str (assoc (dissoc rslt :datetime)
                                                   :datetime dt))))
            (du/update-last-msg-seen to (:id (last rslt)))))))))

(defn connect! [channel id]
  (swap! channels assoc channel id))

(defn disconnect! [channel status]
  (swap! channels dissoc channel))

(defn ws-handler [request]
  (let [id (Integer/parseInt (:id (:params request)))]
    (with-channel request channel
      (connect! channel id)
      (on-close channel (partial disconnect! channel))
      (on-receive channel #(send-msg %)))))

(defroutes websocket-routes
  (GET "/ws/:id" request (ws-handler request)))
