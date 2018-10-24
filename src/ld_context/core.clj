(ns ld-context.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [ld-context.context :as cx])
  (:gen-class))

(def cli-options
  [["-o" "--output OUTPUT-FILE" "Output file"
    :default "context.jsonld"]])

(defn -main [& args]
  (let [opts (parse-opts args cli-options)]
    (clojure.pprint/pprint opts)
    (cx/write-context-from-owl (get-in opts [:options :output])
                               (:arguments opts))))
