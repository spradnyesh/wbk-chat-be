(ns wbk-chat-be.routes.auth
  (:require [wbk-chat-be.layout :as l]
            [wbk-chat-be.db.users :as du]
            [bouncer.core :as bc]
            [bouncer.validators :as bv]
            [buddy.hashers :as bh]
            [compojure.core :refer [defroutes GET POST]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Logic

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Routes

(defroutes auth-routes
  (POST "/register" [email passwd1 passwd2 firstname lastname]
        (register email passwd1 passwd2 firstname lastname)))
