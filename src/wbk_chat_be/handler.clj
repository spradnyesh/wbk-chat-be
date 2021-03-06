(ns wbk-chat-be.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [wbk-chat-be.cron :refer [restart-cron]]
            [wbk-chat-be.layout :refer [error-page]]
            [wbk-chat-be.routes.home :refer [home-routes]]
            [wbk-chat-be.routes.auth :refer [auth-routes]]
            [wbk-chat-be.routes.messages :refer [msg-routes]]
            [wbk-chat-be.routes.reports :refer [report-routes]]
            [wbk-chat-be.routes.websockets :refer [websocket-routes]]
            [wbk-chat-be.middleware :as middleware]
            [wbk-chat-be.db.users :as du]
            [clojure.tools.logging :as log]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [wbk-chat-be.config :refer [defaults]]
            [mount.core :as mount]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (when-let [config (:log-config env)]
    (org.apache.log4j.PropertyConfigurator/configure config))
  (doseq [component (:started (mount/start))]
    (log/info component "started"))
  (du/init-state)
  (restart-cron)
  ((:init defaults)))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log/info "wbk-chat-be is shutting down...")
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (log/info "shutdown complete!"))

(def app-routes
  (routes
   (wrap-routes #'home-routes middleware/wrap-csrf)
   auth-routes msg-routes report-routes websocket-routes
   (route/resources "/")
   (route/not-found
    (:body
     (error-page {:status 404
                  :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
