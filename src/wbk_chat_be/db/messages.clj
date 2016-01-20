(ns wbk-chat-be.db.messages
  (:require [wbk-chat-be.db.core :as db]
            [wbk-chat-be.db.users :as du]
            [clojure.java.io :as io]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Interfaces
;; mostly (thin) wrapper around db/*
;; but needed so that, even if DB changes, these interfaces do not change

(defn send-msg [from to msg]
  (db/create-message<! {:from from :to to
                        :message msg :file ""}))

(defn send-file [from to filename]
  (db/create-message<! {:from from :to to :message ""
                        :file filename}))

(defn read-msgs [user-id msg-id]
  (db/read-user-messages {:id user-id :msgid msg-id}))

(defn rm-old-msgs [date id]
  (let [user (du/find-user-by-id id)
        last-msg-seen (or (:last-msg-seen user) 0)
        params {:to_date date :msg_id last-msg-seen :id id}
        files (db/get-old-user-files params)]
    (map (comp (fn [f] (try (io/delete-file f)
                            (catch java.io.IOException ioe nil)))
               :file) files)
    (db/rm-old-msgs! params)))
