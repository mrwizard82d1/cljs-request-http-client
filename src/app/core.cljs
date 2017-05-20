(ns app.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(def ^:private js-request (nodejs/require "request"))

(defn main [& args]
  (println "Abracadabra!")
  (println js-request))

(set! *main-cli-fn* main)
