(ns wbk-chat-be.cron
  (:require [cronj.core :as cj]
            [wbk-chat-be.dates :as d]
            [wbk-chat-be.db.users :as du]
            [wbk-chat-be.db.messages :as dm]
            [wbk-chat-be.db.reports :as dr]))

(defonce cjt (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; cron tasks

(defn generate-reports [t opts]
  (let [today (d/today)
        year (d/year today)
        week (d/week today)
        from (d/date->sql (d/n-days-ago today 7))
        to (d/date->sql (d/n-days-ago today 1))]
    (doall (map (comp #(dr/set-reports year week from to %) :id)
                (du/get-all-users)))))

(defn rm-msgs-and-files [t opts]
  (let [to-date (d/date->sql (d/n-days-ago (d/today) 7))]
    (doall (map (comp #(dm/rm-old-msgs to-date %) :id)
                (du/get-all-users)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; http://docs.caudate.me/cronj/#crontab

(def cron-jobs ; all times in Asia/Kolkata (due to jvm-opts in project.clj)
  (cj/cronj :entries [{:id :generate-reports
                       :handler generate-reports
                       :schedule "0 0 1 0 * * *" ; 1am sunday
                       :opts {}}
                      {:id :rm-msgs-and-files
                       :handler rm-msgs-and-files ; 5am sunday (after reports have been generated)
                       :schedule "0 0 5 0 * * *"
                       :opts {}}]))

(defn restart-cron []
  (reset! cjt (cj/restart! cron-jobs)))
