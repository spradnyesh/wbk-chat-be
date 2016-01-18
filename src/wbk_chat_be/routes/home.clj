(ns wbk-chat-be.routes.home
   (:require [wbk-chat-be.layout :as layout]
                          [compojure.core :refer [defroutes GET]]
                          [ring.util.http-response :refer [ok]]
                          [clojure.java.io :as io]))

(defn home-page [] (layout/render "index.html"))
(defroutes home-routes (GET "/" [] (home-page)))
