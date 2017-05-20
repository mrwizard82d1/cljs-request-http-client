(ns app.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

;; Keep a private reference to the `request` package
(def ^:private js-request (nodejs/require "request"))


(defn request 
  "Request an internet resource.

  `url-or-opts` Specifies the resource using a URL or a map allowing one to specify additional request 
  details. For more details, see https://github.com/request/request#requestoptions-callback.
  `callback` A function of three arguments to invoke when the request completes.
      `error` Error typically when making a request
      `response` The response for the request
      `body` The body of the response."
  [url-or-opts callback]
  (js-request (clj->js url-or-opts) callback))

;; Simply to test the `request` function
(defn test-request []
  (request {;; Issue an HTTP GET for the resource identified by `:uri`
            :method "GET"
            :uri "http://www.google.com"}
           (fn [error response body]
             (when (and (not error) (= 200 (.-statusCode response)))
               (println body)))))

(defn main [& args]
  (println "Abracadabra!") ; just for "debugging"
  (test-request))

(set! *main-cli-fn* main)
