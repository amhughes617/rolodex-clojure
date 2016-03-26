(ns rolodex-clojure.core
    (:require [clojure.string :as str]
              [clojure.walk :as walk]
              [compojure.core :as c]
              [ring.adapter.jetty :as j]
              [ring.middleware.params :as p]
              [ring.util.response :as r]
              [hiccup.core :as h])
  (:gen-class))

(def contacts (atom []))

(defn pop-contacts []
  (reset! contacts (read-string (slurp "contacts.edn"))))

(defn contacts-html []
  [:ol       
   (map (fn [contact]
          [:li (:name contact) " " (:phone contact)])
     @contacts)])
    
(c/defroutes app
  (c/GET "/" request
    (h/html [:html
             [:body
              [:form {:action "/add-contact" :method "post"}
               [:input {:type "text" :placeholder "Contact name" :name "name"}]
               [:input {:type "text" :placeholder "Phone number(xxx-xxx-xxxx)" :name "phone"}]
               [:button {:type "submit"} "Submit"]]
              (contacts-html)]]))

  (c/POST "/add-contact" request
    (let [params (:params request)
          name (get params "name")
          phone (get params "phone")
          contact (hash-map :name name :phone phone)]
      (swap! contacts conj contact))
    (spit "contacts.edn" (pr-str @contacts))
    (r/redirect "/")))

(defn -main []
  (try (when (empty? @contacts) (pop-contacts))
    (catch Exception _))
  (j/run-jetty (p/wrap-params app) {:port 3000}))