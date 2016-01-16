(ns wbk-chat-be.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[wbk-chat-be started successfully]=-"))
   :middleware identity})
