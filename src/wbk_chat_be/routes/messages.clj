(ns wbk-chat-be.routes.messages
  (:require [wbk-chat-be.layout :as l]
            [wbk-chat-be.db.users :as du]
            [wbk-chat-be.db.messages :as dm]
            [compojure.core :refer [defroutes GET POST]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Helpers

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic

(defn send-msg [token to msg]
  (try (let [to-id (Integer/parseInt to)
             from (du/find-user-by-token token)]
         (if-let [msg-id (dm/send-msg (:id from) to-id msg)]
           (do nil ; TODO: send msg to to-id via websockets
               (l/json {:status true :body msg-id}))
           (l/json {:status nil :body "Could not send message!"})))
       (catch NumberFormatException nfe
         (l/json {:status nil :body "Invalid To-User!"}))))

(defn sync-msgs [token last-msg-id]
  (try (let [msg-id (Integer/parseInt (or last-msg-id "0"))
             user (du/find-user-by-token token)
             msgs (dm/read-msgs (:id user)
                                (or msg-id (:last-msg-seen user)))]
         (du/update-last-msg-seen token (:id (last msgs)))
         (l/json {:status true
                  :body (mapv #(let [from (du/get-user (:from_user_id %))
                                     to (du/get-user (:to_user_id %))]
                                 (assoc %
                                        :from-first-name (:first_name from)
                                        :from-last-name (:last_name from)
                                        :to-first-name (:first_name to)
                                        :to-last-name (:last_name to)))
                              msgs)}))
       (catch NumberFormatException nfe
         (sync-msgs token "0"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Routes

(defroutes msg-routes
  (POST "/send" [token to msg] (send-msg token to msg))
  (GET "/sync/:token/:msg-id" [token msg-id] (sync-msgs token msg-id)))
