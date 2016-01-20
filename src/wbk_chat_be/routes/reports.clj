(ns wbk-chat-be.routes.reports
  (:require [wbk-chat-be.layout :as l]
            [wbk-chat-be.db.users :as du]
            [wbk-chat-be.db.reports :as dr]
            [compojure.core :refer [defroutes GET]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic

(defn reports []
  (if-let [reports (dr/get-reports)]
    (l/json {:status true :body reports})
    (l/json {:status nil :body "Could not get reports!"})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Routes

(defroutes report-routes
  (GET "/reports" request (reports)))
