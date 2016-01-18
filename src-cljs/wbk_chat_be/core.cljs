(ns wbk-chat-be.core
  (:require [reagent.core :as r]
            [secretary.core :as sec :refer-macros [defroute]]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; global app-state
;; whenever this value changes our "main" component gets re-drawn

(defonce app-state (r/atom {:logged-in nil
                            :users []
                            :messages []}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; utils

(defn by-id [id] (.getElementById js/document id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; main UI components

(defn nav []
  (if-not (:logged-in @app-state)
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li [:a.navbar-brand {} "WBK-Chat"]]
      [:li [:a {} "Sign-up"]]
      [:li [:a {} "/"]]
      [:li [:a {} "Sign-in"]]]]
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li [:a.navbar-brand {} "WBK-Chat"]]
      [:li [:a {} "Log-out"]]]]))

(defn main
  "displays UI using 'hiccup' style markup"
  []
  [:div.container
   [:div.row {:id "upper"}
    [:div.col-sm-8 "Messages"]
    [:div.col-sm-4 "Users"]]
   [:div.row
    [:div.col-sm-8 "Input"]
    [:div.col-sm-4 "Send"]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; init system

(defn mount-components
  "init function:
  - initializes global state
  - attaches component 'main' to '#app' element in index.html"
  []
  (r/render [nav] (by-id "nav"))
  (r/render [main] (by-id "app")))

(defn init! [] (mount-components))
