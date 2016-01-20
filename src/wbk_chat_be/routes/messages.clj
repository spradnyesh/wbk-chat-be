(ns wbk-chat-be.routes.messages
  (:require [wbk-chat-be.layout :as l]
            [wbk-chat-be.db.users :as du]
            [wbk-chat-be.db.messages :as dm]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.string :as str]
            [clojure.java.io :as io]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Helpers

(defn rename-file [src dest]
  (let [name (str/join "" (drop-last 1 (str/split (last (str/split src #"/")) #"\.")))
        extn (last (str/split dest #"\."))
        new-fname (str "resources/public/uploads/" name "." extn)]
    (try (io/copy (io/file src) (io/file new-fname))
         (io/delete-file src)
         new-fname
         (catch Exception e nil))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic

(defn send-msg [token to msg]
  (let [from (du/find-user-by-token token)]
    (if-let [rslt (dm/send-msg (:id from) to msg)]
      (do nil ; TODO: send msg to to-id via websockets
          (l/json {:status true :body rslt}))
      (l/json {:status nil :body "Could not send message!"}))))

(defn sync-msgs [token last-msg-id]
  (try (let [msg-id (Integer/parseInt (or last-msg-id "0"))
             user (du/find-user-by-token token)
             msgs (dm/read-msgs (:id user)
                                (or msg-id (:last-msg-seen user)))]
         (du/update-last-msg-seen token (:id (last msgs)))
         (l/json {:status true
                  :body (if (zero? msg-id)
                          msgs
                          (remove #(= (:id user) (:from_user_id %))
                                  msgs))}))
       (catch NumberFormatException nfe
         (sync-msgs token "0"))))

(defn share [token to {:keys [filename size tempfile] :as file}]
  (if (> size (* 20 1024 1024))
    (l/json {:status nil :body "File size cannot be bigger than 20MB!"})
    (let [from (du/find-user-by-token token)
          to-id (Integer/parseInt to)
          new-fname (rename-file (str tempfile) (str filename))]
      (if new-fname
        (if-let [rslt (dm/send-file (:id from) to-id new-fname)]
          (do nil ; TODO: send msg to to-id via websockets
              (l/json {:status true :body rslt}))
          (l/json {:status nil :body "Could not share file!"}))
        (l/json {:status nil :body "Could not store file!"})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Routes

(defroutes msg-routes
  (POST "/send-msg" [token to msg] (send-msg token to msg))
  (GET "/sync" [token msg-id] (sync-msgs token msg-id))
  (POST "/share" [token to file] (share token to file)))
