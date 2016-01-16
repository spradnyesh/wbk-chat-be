(ns wbk-chat-be.db.users
  (:require [wbk-chat-be.db.core :as db]))

(defn get-user [id]
  (first (db/get-user {:id id})))

(defn find-user [email]
  (first (db/find-user {:email email})))

(defn create-user [user]
  (db/create-user! user))
