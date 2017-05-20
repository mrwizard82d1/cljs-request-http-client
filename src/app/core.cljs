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

;; The rub with callbacks is that all  the work must be done at the leaves of the calling tree. 
;; It does not seem to support inversion of control (although I might mimic this by passing additional
;; callbacks through the calling tree).
(defn- when-response-received [error response body]
    ;; Separate this response from others - but how do I print the request?
    (println)
    (println)
  (cond 
    ;; Print the body if no error and HTTP status code 200
    (and (not error) (= 200 (.-statusCode response))) 
    (println body)
    ;; Print the error and, optionally, the status code if available.
    error 
    (do (println "Error requesting resource") 
        (println "  Name:" (.-name error))
        (println "  Message:" (.-message error))
        (when response
          (println "HTTP status:"(.-statusCode response))))
    ;; Print the status code and body if not HTTP status code 200
    :else
    (do (println "HTTP status: "(.-statusCode response))
        (println "Non-200 body")
        (println body))))

;; Simply to test the `request` function
;;
;; Callbacks means that I cannot return the response to the caller....
;; but must handle the details in the callback.
(defn test-requests []
  (println "The requests may *not* print in the order issued. Asynchrony.")
  (println)
  (doseq [uri ["http://httpbin.org/html" 
               "http://httpbin.org/redirect-to?url=http://httpbin.org/get"
               "http://httpbin.org/htm"
               "http://mollint@trepiatis@int"]]
    (request {:method "GET"
              :uri uri}
             when-response-received)))

(defn main [& args]
  (println "Abracadabra!") ; just for "debugging"
  (test-requests))

(set! *main-cli-fn* main)
