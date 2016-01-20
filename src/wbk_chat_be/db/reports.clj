(ns wbk-chat-be.db.reports
  (:require [wbk-chat-be.db.core :as db]
            [wbk-chat-be.db.messages :as dm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Interfaces
;; mostly (thin) wrapper around db/*
;; but needed so that, even if DB changes, these interfaces do not change

(defn set-reports [year week from-dt to-dt id]
  (let [params {:id id :from from-dt :to to-dt}
        messages-from (:count (first (db/count-messages-from params)))
        messages-to (:count (first (db/count-messages-to params)))
        shares-from (:count (first (db/count-shares-from params)))
        shares-to (:count (first (db/count-shares-to params)))]
    (db/create-user-report! {:id id :year year :week week
                             :num_msg_sent messages-from
                             :num_msg_recd messages-to
                             :num_vid_sent shares-from
                             :num_vid_recd shares-to})))
