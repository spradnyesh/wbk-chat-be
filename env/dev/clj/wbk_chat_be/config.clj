(ns wbk-chat-be.config
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [wbk-chat-be.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[wbk-chat-be started successfully using the development profile]=-"))
   :middleware wrap-dev})
