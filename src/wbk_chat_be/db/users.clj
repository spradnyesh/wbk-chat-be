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
;;;; Interfaces
;; mostly (thin) wrapper around db/*
;; but needed so that, even if DB changes, these interfaces do not change

(defn get-all-users []
  (db/get-all-users))

(defn get-user [id]
  (first (db/get-user {:id id})))

(defn find-user-by-email [email]
  (first (db/find-user {:email email})))

(defn find-user-by-token [token]
  (last (find-in-state :token token)))

(defn init-state []
  (reset! state
          (mapv (fn [u] {:id (:id u) :email (:email u)
                         :token nil :last-msg-seen 0})
                (get-all-users))))

(defn update-token [email token]
  (if-let [idx (first (find-in-state :email email))]
    (swap! state assoc-in [idx :token] token)
    (let [user (find-user-by-email email)]
      (swap! state conj {:id (:id user) :email email
                         :token token :last-msg-seen 0}))))

(defn update-last-msg-seen [token msgid]
  (if-let [idx (first (find-in-state :token token))]
    (swap! state assoc-in [idx :last-msg-seen] msgid)
    (let [user (find-user-by-token token)]
      (swap! state conj {:id (:id user) :email (:email user)
                         :token token :last-msg-seen 0}))))

(defn create-user [user]
  (db/create-user<! user)
  (update-token (:email user) nil))
