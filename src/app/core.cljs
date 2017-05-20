(ns app.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(def ^:private js-request (nodejs/require "request"))

(defn request [url-or-opts callback]
  (js-request (clj->js url-or-opts) callback))

(defn test-request []
  (request {:method "GET"
            :uri "http://www.google.com"}
           (fn [error response body]
             (when (and (not error) (= 200 (.-statusCode response)))
               (println body)))))

(defn main [& args]
  (println "Abracadabra!")
  (test-request))

(set! *main-cli-fn* main)
