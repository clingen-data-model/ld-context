(defproject ld-context "0.1.0-SNAPSHOT"
  :description "JSON-LD context generator from OWL source files"
  :url "https://github.com/clingen-data-model/ld-context"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.apache.jena/jena-core "3.9.0"]]
  :main ^:skip-aot ld-context.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
