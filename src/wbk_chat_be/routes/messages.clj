(ns wbk-chat-be.routes.messages
  (:require [wbk-chat-be.layout :as l]
            [wbk-chat-be.db.users :as du]
            [wbk-chat-be.db.messages :as dm]
            [wbk-chat-be.routes.websockets :as ws]
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

(defn sync-msgs [token last-msg-id]
  (let [user (du/find-user-by-token token)
        msg-id (try (Integer/parseInt last-msg-id)
                    (catch NumberFormatException nfe
                      (or (:last-msg-seen user) 0)))
        msgs (dm/read-msgs (:id user) msg-id)]
    (du/update-last-msg-seen token (:id (last msgs)))
    (l/json {:status true :body msgs})))

(defn sync-vids [token last-msg-id]
  (let [user (du/find-user-by-token token)
        msg-id (try (Integer/parseInt last-msg-id)
                    (catch NumberFormatException nfe
                      (or (:last-msg-seen user) 0)))
        msgs (dm/read-vids (:id user) msg-id)]
    (l/json {:status true :body msgs})))

(defn share [token to {:keys [filename content-type tempfile size]}]
  (if (> size (* 20 1024 1024))
    (l/json {:status nil :body "File size cannot be bigger than 20MB!"})
    (let [from (du/find-user-by-token token)
          to-id (Integer/parseInt to)
          new-fname (rename-file (str tempfile) (str filename))]
      (if new-fname
        (if-let [rslt (dm/send-file (:id from) to-id new-fname)]
          (l/json {:status true :body rslt})
          (l/json {:status nil :body "Could not share file!"}))
        (l/json {:status nil :body "Could not store file!"})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Routes

(defroutes msg-routes
  (GET "/sync" [token msg-id] (sync-msgs token msg-id))
  (GET "/sync-vids" [token msg-id] (sync-vids token msg-id))
  (POST "/share" [token to file] (share token to file)))
