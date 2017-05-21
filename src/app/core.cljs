(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async])
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
  ;; `go` asynchronousely executes its body
  (async-m/go 
    ;; Retrieve a result from the channel returned by `request`
    (let [result (async/<! (request "http://httpbin.org/html"))]
      (cond 
        ;; If channel contains an error, report it
        (:error result) (println (:error result))
        ;; Otherwise, print the HTTP status code and the HTTP body
        :else (do
                (println (.-statusCode (:response result)))
                (println (:body result)))))))


(set! *main-cli-fn* main)
