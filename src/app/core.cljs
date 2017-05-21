(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async]
            [cljs.core.match :refer-macros [match]])
  (:require-macros [cljs.core.async.macros :as async-m]))

(nodejs/enable-util-print!)

;; The private `request` module
(def ^:private js-request (nodejs/require "request"))

(defn request 
  "Request an internet resource.

  `url-or-opts` Specifies the resource using a URL or a map allowing one to specify additional request 
  details. For more details, see https://github.com/request/request#requestoptions-callback.

  This function returns a channel for asynchronous reads and writes. The (local) callback function will
  write the response to this channel when available. The caller can read the response at her leisure."
  [url-or-opts]
  ;; Create a channel capable of buffering a single item
  (let [channel (async/chan 1)]
    ;; Make the HTTP request using the `request` package
    (js-request (clj->js url-or-opts)
                (fn [error response body]
                  ;; The callback asynchronously puts the result on the channel
                  (async/put! channel 
                              (if error 
                                ;; If an error occurred, put the error details
                                {:error error}
                                ;; Otherwise, put the response (headers) and the body on the channel
                                {:response response
                                 :body body})
                              ;; When complete, close the channel.
                              #(async/close! channel))))
    channel))

(defn main [& args]
  (println "Abracadabra!") ; Just for debugging
  (doseq [url ["http://httpbin.org/html" 
               "http://httpbin.org/redirect-to?url=http://httpbin.org/get"
               "http://httpbin.org/htm"
               "http://mollint@trepiatis@int"]]
    ;; `go` asynchronousely executes its body
    (async-m/go 
      ;; Retrieve a result from the channel returned by `request`
      (match [(async/<! (request url))]
             [{:error error}] (do (println "Error requesting" url)
                                  (println "  Name:" (.-name error))
                                  (println "  Message" (.-message error)))
             [{:response response-headers :body body}] (do 
                                                         (println "HTTP status:" 
                                                                  (.-statusCode response-headers))
                                                         (println "HTTP body:" 
                                                                  body))))))

(set! *main-cli-fn* main)
