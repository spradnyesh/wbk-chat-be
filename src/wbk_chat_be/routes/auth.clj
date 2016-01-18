(ns wbk-chat-be.routes.auth
  (:require [wbk-chat-be.layout :as l]
            [wbk-chat-be.db.users :as du]
            [bouncer.core :as bc]
            [bouncer.validators :as bv]
            [buddy.hashers :as bh]
            [compojure.core :refer [defroutes GET POST]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Helpers

(defn valid? [data]
  (let [rslt (bc/validate data
                          :email bv/required
                          :email bv/email
                          :passwd bv/required
                          :passwd2 bv/required)]
    (if-not (= (:passwd data) (:passwd2 data))
      (update-in rslt [0]
                 assoc :passwd '("Both passwords do not match."))
      rslt)))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Token

(def characters "0123456789abcdefghijklmnopqrstuvwxyz")

(defn gen-token
  "generates and stores a 'token'
  that will be used to identify a user in all calls henceforth"
  [email]
  (let [token (->> (repeatedly #(rand-nth characters))
                   (take 30)
                   (apply str)
                   ;; may need to do something to reduce collisions
                   ;; as #users increase
                   #_((str email)
                      bh/encrypt ; maybe some hashing (md5, etc)
                      (take-last 30)
                      (apply str)))]
    (du/update-token email token)))

(defn del-token
  "removes 'token' from storage if it exists"
  [token]
  (when-let [email (:email (last (du/find-in-state :token token)))]
    (du/update-token email nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic

(defn register [email passwd passwd2 firstname lastname]
  (if (du/find-user email)
    (l/render {:status nil
               :body "User already exists!"})
    (let [user {:email email
                :passwd passwd
                :passwd2 passwd2
                :firstname firstname
                :lastname lastname}]
      (let [v (valid? user)]
        (if-not (first v)
          (do (du/create-user (assoc user
                                     :passwd (bh/encrypt passwd)))
              (l/render {:status true
                         :body "Registered sucessfully!"}))
          (l/render {:status nil
                     :body (first v)}))))))

(defn login [email passwd]
  (if-not (du/find-user email)
    (l/render {:status nil
               :body "Invalid username/password!"})
    (do (l/render {:status true
                   :body {:token (gen-token email)
                          :users (mapv #(select-keys % [:first_name :last_name])
                                       (du/get-all-users))}}))))

(defn logout [token]
  (if (del-token token)
    (l/render {:status true :body "Logged-out successfully!"})
    (l/render {:status nil :body "Invalid token!"})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Routes

(defroutes auth-routes
  (POST "/register" [email passwd1 passwd2 firstname lastname]
        (register email passwd1 passwd2 firstname lastname))
  (POST "/login" [email passwd] (login email passwd))
  (POST "/logout" [token] (logout token)))
