(ns wbk-chat-be.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; global states
;; whenever these value changes our "main" component gets re-drawn

(defonce app-state (r/atom {:token nil
                            :users []
                            :messages []}))
(defonce page (r/atom :home))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; utils

(defn by-id [id] (.getElementById js/document id))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something terrible happened: " status " : " status-text)))

(defn event->form-children [e]
  (.-children (.-form (.-target e))))

(defn form-child-value [children index]
  (.-value (aget (.-children (aget children index)) 1)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Handlers

(defn h-register [response])

(defn h-login [response])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic (communication with service)

(defn register [e]
  (let [f-children (event->form-children e)
        email (form-child-value f-children 0)
        passwd (form-child-value f-children 1)
        passwd2 (form-child-value f-children 2)
        firstname (form-child-value f-children 3)
        lastname (form-child-value f-children 4)]
    #_(POST "/register" {:handler h-register
                       :error-handler error-handler
                       :params {:email email
                                :passwd passwd
                                :passwd2 passwd2
                                :firstname firstname
                                :lastname lastname}})))

(defn login [e]
  (let [f-children (event->form-children e)
        email (form-child-value f-children 0)
        passwd (form-child-value f-children 1)]
    #_(POST "/login" {:handler h-login
                    :error-handler error-handler
                    :params {:email email
                             :passwd passwd}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; main UI components

(defn register-form []
  [:div.container
   [:form
    [:div.form-group
     [:label {:for "email"} "Email"]
     [:input.form-control {:type "email" :id "email"}]]
    [:div.form-group
     [:label {:for "password"} "Password"]
     [:input.form-control {:type "password" :id "passwd"}]]
    [:div.form-group
     [:label {:for "password"} "Password Again"]
     [:input.form-control {:type "password" :id "passwd2"}]]
    [:div.form-group
     [:label {:for "firstname"} "First Name"]
     [:input.form-control {:type "text" :id "firstname"}]]
    [:div.form-group
     [:label {:for "lastname"} "Last Name"]
     [:input.form-control {:type "text" :id "lastname"}]]
    [:div.form-group
     [:button.btn.btn-default {:type "submit"
                               :on-click register} "Register"]]]])

(defn login-form []
  [:div.container
   [:form
    [:div.form-group
     [:label {:for "email"} "Email"]
     [:input.form-control {:type "email" :id "email"}]]
    [:div.form-group
     [:label {:for "password"} "Password"]
     [:input.form-control {:type "password" :id "passwd"}]]
    [:div.form-group
     [:button.btn.btn-default {:type "submit"
                               :on-click login} "Login"]]]])

(defn nav []
  (if-not (:token @app-state)
    ;; logged-out state
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li [:a.navbar-brand {:href "#" :on-click #(reset! page :home)}
            "WBK-CHAT"]]
      [:li [:a {:href "#" :on-click #(reset! page :sign-up)}
            "Sign-up"]]
      [:li [:a {:href "#" :on-click #(reset! page :sign-in)}
            "Sign-in"]]]]
    ;; logged-in state
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li [:a.navbar-brand {} "WBK-CHAT"]]
      [:li [:a {} "Log-out"]]]]))

(defn home [] [:div.jumbotron [:h2 "Welcome! to Wunderbar Kids!!!"]])

(defn chat []
  [:div
   [:div.row {:id "upper"}
    [:div.col-sm-8 "Messages"]
    [:div.col-sm-4 "Users"]]
   [:div.row
    [:div.col-sm-8 "Input"]
    [:div.col-sm-4 "Send"]]])

(defn main
  "displays UI using 'hiccup' style markup"
  [state]
  [:div.container (condp = @page
                    :home (home)
                    :sign-up (register-form)
                    :sign-in (login-form)
                    :chat (chat))])

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
