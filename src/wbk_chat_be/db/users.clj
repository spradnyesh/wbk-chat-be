(ns wbk-chat-be.db.users
  (:require [wbk-chat-be.db.core :as db]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; State
;; may be replaced by memcached/redis in a real app

(defonce state (atom []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Helpers

(defn find-in-state [key value]
  (first (for [i (range (count @state))
               :when (= value (key (nth @state i)))]
           [i (nth @state i)])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Manage state

(defn init-state []
  (reset! state
          (mapv (fn [u] {:email (:email u) :token nil})
                (get-all-users))))

(defn update-token [email token]
  (if-let [idx (first (find-in-state :email email))]
    (swap! state assoc-in [idx :token] token)
    (swap! state conj {:email email :token token})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Interfaces
;; mostly (thin) wrapper around db/*
;; but needed so that, even if DB changes, these interfaces do not change

(defn get-all-users []
  (db/get-all-users))

(defn get-user [id]
  (first (db/get-user {:id id})))

(defn find-user [email]
  (first (db/find-user {:email email})))

(defn create-user [user]
  (db/create-user! user))
