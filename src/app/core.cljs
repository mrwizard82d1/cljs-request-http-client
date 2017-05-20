(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :as async-m]))

(nodejs/enable-util-print!)

(def ^:private js-request (nodejs/require "request"))

(defn request 
  "Request an internet resource.

  `url-or-opts` Specifies the resource using a URL or a map allowing one to specify additional request 
  details. For more details, see https://github.com/request/request#requestoptions-callback.

  This function returns a channel for asynchronous reads and writes. The (local) callback function will
  write the response to this channel when available. The caller can read the response at her leisure."
  [url-or-opts]
  (let [channel (async/chan 1)]
    (js-request (clj->js url-or-opts)
                (fn [error response body]
                  (async/put! channel 
                              (if error 
                                {:error error}
                                {:response response
                                 :body body})
                              #(async/close! channel))))
    channel))

(defn main [& args]
  (println "Abracadabra!")
  (async-m/go 
    (let [result (async/<! (request "http://httpbin.org/html"))]
      (cond (:error result) (println (:error result))
            :else (do
                    (println (.-statusCode (:response result)))
                    (println (:body result)))))))


(set! *main-cli-fn* main)
