(ns wbk-chat-be.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [wbk-chat-be.websockets :as ws]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; global states
;; whenever these value changes our "main" component gets re-drawn

(defonce app-state (r/atom {:id nil
                            :token nil
                            :name nil
                            :users []
                            :to nil
                            :to-name nil
                            :last-msg-seen 0
                            :popup nil}))
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

(defn close-popup [e] (swap! app-state assoc :popup nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Response Handlers

(defn h-share [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (do (swap! messages conj {:to (body "to_user_id")
                                :file (body "file")
                                :datetime (body "datetime")})
          (swap! app-state assoc :popup nil))
      (swap! app-state assoc
             :popup [:div [:p "File sharing failed!"]
                     [:p (str "Reason: " body)]
                     [:p "Please try again."]
                     [:button.btn.btn-default
                      {:on-click close-popup}
                      "OK!"]]))))

(defn h-sync [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (when-not (empty? body)
        (doall (map (fn [b]
                      (swap! messages conj {:to (b "to_user_id")
                                            :from (b "from_user_id")
                                            :msg (b "message")
                                            :file (b "file")
                                            :datetime (b "datetime")}))
                    body))
        (swap! app-state assoc :last-msg-seen ((last body) "id"))))))

(defn h-register [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (do (js/alert (str body ". You can Sign-in now!"))
          (reset! page :home))
      (js/alert (str body " Please try again.")))))

(defn h-send-msg [msg]
  (swap! messages conj {:from (:to @app-state)
                        :msg (msg "message")
                        :datetime (msg "datetime")}))

(defn h-login [response]
  (let [status (response "status")
        body (response "body")]
    (if status
      (let [users (body "users")
            first-user (first users)]
        (swap! app-state assoc
               :id (body "id")
               :token (body "token")
               :name (body "name")
               :users users
               :to ((first users) "id")
               :to-name (str (first-user "first_name")
                             " " (first-user "last_name")))
        (reset! page :home)
        (ws/make-websocket! (str "ws://" (.-host js/location) "/ws/" (:id @app-state))
                            h-send-msg))
      (js/alert (str body " Please try again.")))))

(defn h-logout [response]
  (let [status (response "status")
        body (response "body")]
    (reset! app-state nil)
    (reset! messages [])
    (reset! page :home)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic (communication with service)

(defn sync-msgs []
  (when-let [token (:token @app-state)]
    (GET "/sync" {:handler h-sync
                  :error-handler error-handler
                  :format :json
                  :params {:token token
                           :msg-id (or (:last-msg-seen @app-state) 0)}})))

(defn send-msg [msg]
  (let [params {"token" (:token @app-state)
                "to" (:to @app-state)
                "msg" msg}]
    (swap! messages conj {:to (:to @app-state)
                          :msg msg
                          :datetime ""})
    (ws/send-transit-msg! params)))

(defn upload-file [e]
  (let [el (by-id "file")
        fname (.-name el)
        file (aget (.-files el) 0)
        form-data (doto (js/FormData.)
                    (.append "token" (:token @app-state))
                    (.append "to" (:to @app-state))
                    (.append fname file))]
    (POST "/share" {:handler h-share
                    :error-handler error-handler
                    :format :json
                    :body form-data})))

(defn share [e]
  (swap! app-state assoc
         :popup [:form
                 [:input {:id "file" :name "file"
                          :type "file" :accept "video/*"}]
                 [:input.btn.btn-default {:type "button"
                                          :on-click upload-file
                                          :value "Share!"}]
                 [:input.btn.btn-default {:type "button"
                                          :on-click close-popup
                                          :value "Cancel"}]]))

(defn register [e]
  (let [f-children (event->form-children e)
        email (child-value f-children 0)
        passwd (child-value f-children 1)
        passwd2 (child-value f-children 2)
        firstname (child-value f-children 3)
        lastname (child-value f-children 4)]
    (POST "/register" {:handler h-register
                       :error-handler error-handler
                       :format :json
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
                    :format :json
                    :params {:email email
                             :passwd passwd}})))

(defn logout [e]
  (POST "/logout" {:handler h-logout
                   :error-handler error-handler
                   :format :json
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
  (when (not (empty? @messages))
    [:table.table.table-striped
     [:tbody
      (doall (for [[i msg] (map-indexed vector (reverse @messages))]
               ^{:key i}
               [:tr [:td [:ul.list-unstyled
                          [:li.small [:span.datetime (:datetime msg)]
                           (when-let [to (:to msg)]
                             [:span.bg-success.pull-right (user-id->name to)])
                           (when-let [from (:from msg)]
                             [:span.bg-info.pull-right (user-id->name from)])]
                          [:li (cond (not (empty? (:msg msg)))
                                     [:mark (:msg msg)]

                                     (:file msg)
                                     [:video {:width "80%" :controls true}
                                      [:source {:src (subs (:file msg) 16)}]])]]]]))]]))

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

(defn popup [markup]
  (if-let [markup (:popup @app-state)]
    [:div.show markup]
    [:div.hidden]))

(defn chat []
  [:div
   [:div.row
    [:div.col-sm-10
     [:div [:h5 "Currently chatting with " [:strong (:to-name @app-state)]]]
     [message-input]]
    [:div.col-sm-2 [:button.btn.btn-default {:on-click share}
                    "Share video"]]]
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
  (r/render [main] (by-id "app"))
  (r/render [popup [:div]] (by-id "popup"))
  #_(js/setInterval sync-msgs 1000))

(defn init! [] (mount-components))

;; need to be called explicitly in non-dev environments
(init!)
