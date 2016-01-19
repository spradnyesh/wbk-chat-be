(ns wbk-chat-be.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; global states
;; whenever these value changes our "main" component gets re-drawn

(defonce app-state (r/atom {:token nil
                            :name nil
                            :users []
                            :to nil
                            :to-name nil}))
(defonce page (r/atom :home))
(defonce messages (r/atom []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; utils

(defn by-id [id] (.getElementById js/document id))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something terrible happened: " status " : " status-text)))

(defn event->form-children [e]
  (.-children (.-form (.-target e))))

(defn child-value [children index]
  (.-value (aget (.-children (aget children index)) 1)))

(defn user-id->name [id]
  (when id
    (when-let [u (first (filter #(= id (% "id"))
                                (:users @app-state)))]
      (str (u "first_name") " " (u "last_name")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Handlers

(defn h-send-msg [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (swap! messages conj {:to (:to @app-state)
                            :msg (body "message")
                            :datetime (body "datetime")})
      (js/alert (str body " Please try again.")))))

(defn h-register [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (do (js/alert (str body ". You can Sign-in now!"))
          (reset! page :home))
      (js/alert (str body " Please try again.")))))

(defn h-login [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (do (swap! app-state assoc
                 :token (body "token")
                 :name (body "name")
                 :users (body "users")
                 :to ((first (body "users")) "id"))
          (reset! page :home))
      (js/alert (str body " Please try again.")))))

(defn h-logout [response]
  (let [status (response "status")
        body (response "body")]
    (swap! app-state assoc
           :token nil
           :name nil)
    (reset! page :home)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic (communication with service)

(defn send-msg [msg]
  (POST "/send-msg" {:handler h-send-msg
                     :error-handler error-handler
                     :params {:token (:token @app-state)
                              :to (:to @app-state)
                              :msg msg}}))

(defn register [e]
  (let [f-children (event->form-children e)
        email (child-value f-children 0)
        passwd (child-value f-children 1)
        passwd2 (child-value f-children 2)
        firstname (child-value f-children 3)
        lastname (child-value f-children 4)]
    (POST "/register" {:handler h-register
                       :error-handler error-handler
                       :params {:email email
                                :passwd passwd
                                :passwd2 passwd2
                                :firstname firstname
                                :lastname lastname}})))

(defn login [e]
  (let [f-children (event->form-children e)
        email (child-value f-children 0)
        passwd (child-value f-children 1)]
    (POST "/login" {:handler h-login
                    :error-handler error-handler
                    :params {:email email
                             :passwd passwd}})))

(defn logout [e]
  (POST "/logout" {:handler h-logout
                  :error-handler error-handler
                  :params {:token (:token @app-state)}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; helper UI components

(defn users []
  [:ul.list-unstyled
   (for [[i u] (map-indexed vector (:users @app-state))]
     (let [name (str (u "first_name") " " (u "last_name"))]
       ^{:key i}[:li [:a {:href "#"
                          :on-click #(swap! app-state assoc
                                            :to (u "id")
                                            :to-name name)}
                      name]]))])

(defn message-list []
  [:table.table.table-striped
   (doall (for [[i msg] (map-indexed vector (reverse @messages))]
            ^{:key i}
            [:tr [:td [:ul.list-unstyled
                       [:li [:span.datetime (:datetime msg)]
                            (when-let [to (:to msg)]
                              [:span.bg-success.pull-right (user-id->name to)])
                            (when-let [from (:from msg)]
                              [:span.bg-info.pull-right (user-id->name from)])]
                       [:li.msg (:msg msg)]]]]))])

(defn message-input []
  (let [value (r/atom nil)]
    (fn []
      [:div.panel
       [:input.form-control
        {:type :text
         :placeholder "type in a message and press enter"
         :value @value
         :on-change #(reset! value (-> % .-target .-value))
         :on-key-down
         #(when (= (.-keyCode %) 13)
            (send-msg @value)
            (reset! value nil))}]])))

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
     [:button.btn.btn-default {:type "submit" :on-click register} "Register"]]]])

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
     [:button.btn.btn-default {:type "submit" :on-click login} "Login"]]]])

(defn nav []
  (if-not (:token @app-state)
    ;; logged-out state
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li [:a.navbar-brand {:href "#" :on-click #(reset! page :home)}
            "WBK-CHAT"]]
      [:li [:a {:href "#" :on-click #(reset! page :sign-up)}
            "Register"]]
      [:li [:a {:href "#" :on-click #(reset! page :sign-in)}
            "Login"]]]]
    ;; logged-in state
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li [:a.navbar-brand {:href "#" :on-click #(reset! page :home)}
            "WBK-CHAT"]]
      [:li [:a {:href "#" :on-click nil} (str "Welcome \"" (:name @app-state) "\"!")]]
      [:li [:a {:href "#" :on-click logout} "Logout"]]]]))

(defn chat []
  [:div
   [:div.row
    [:div.col-sm-10
     [:div [:h5 "Currently chatting with " [:strong (:to-name @app-state)]]]
     [message-input]]
    [:div.col-sm-2 [:button.btn.btn-default "Share image/video"]]]
   [:div.row
    [:div.col-sm-10
     [message-list]]
    [:div.col-sm-2 [users]]]])

(defn home []
  (if-not (:token @app-state)
    [:div.jumbotron [:h2 "Welcome to Wunderbar Kids!!!"]]
    [chat]))

(defn main
  "displays UI using 'hiccup' style markup"
  [state]
  [:div.container (condp = @page
                    :home [home]
                    :sign-up [register-form]
                    :sign-in [login-form]
                    :chat [chat])])

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
