(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async]
            [cljs.core.match :refer-macros [match]])
  (:require-macros [cljs.core.async.macros :as async-m]))

(nodejs/enable-util-print!)

;; The private `request` module
(def ^:private js-request (nodejs/require "request"))

;; Define separate channels for successful responses and for error responses
(def success-channel (async/chan 5))
(def error-channel (async/chan 5))

(defn request 
  "Request an internet resource.

  `url-or-opts` Specifies the resource using a URL or a map allowing one to specify additional request 
  details. For more details, see https://github.com/request/request#requestoptions-callback.

  This function returns a channel for asynchronous reads and writes. The (local) callback function will
  write the response to this channel when available. The caller can read the response at her leisure."
  [url-or-opts]
  ;; Create a channel capable of buffering a single item
  (let [all-channels (success-channel error-channel)]
    ;; Make the HTTP request using the `request` package
    (js-request (clj->js url-or-opts)
                (fn [error response body]
                  ;; Put errors and regular responses on *different* channels
                  (if error
                    (async/put! error-channel error)
                    (async/put! success-channel {:response response :body body}))))
    all-channels))

(defn main [& args]
  (println "Abracadabra!") ; Just for debugging
  ;; Issue multiple requests
  (doseq [url ["http://httpbin.org/html" 
               "http://httpbin.org/redirect-to?url=http://httpbin.org/get"
               "http://httpbin.org/htm"
               "http://mollint@trepiatis@int"]]
    (request url))
  ;; `go` asynchronousely executes its body
  (async-m/go 
    (let [[result ch] (async/alts! [success-channel error-channel])]
      (case ch
        success-channel (do 
                          (println "HTTP status:" (.-statusCode (:response result)))
                          (println "HTTP body:" (:body result)))
        error-channel (do (println "Error requesting" url)
                          (println "  Name:" (.-name (:error result)))
                          (println "  Message" (.-message (:error result))))))))

(set! *main-cli-fn* main)
