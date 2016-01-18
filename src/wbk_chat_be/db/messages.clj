(ns wbk-chat-be.db.messages
  (:require [wbk-chat-be.db.core :as db]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Helpers

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Interfaces
;; mostly (thin) wrapper around db/*
;; but needed so that, even if DB changes, these interfaces do not change

(defn send-msg [from to msg]
  (db/create-message! {:from from :to to :message msg}))

(defn read-msgs [user-id msg-id]
  (db/read-user-messages {:id user-id :msgid msg-id}))
