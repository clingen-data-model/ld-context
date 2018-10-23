(ns ld-context.context-test
  (:require [clojure.test :refer :all]
            [ld-context.context :refer :all]))

(deftest true-test
  (testing "this is true")
  (is (= 1 1)))


;; (def m (ModelFactory/createOntologyModel))
;; (.read m (io/input-stream "data/sepio.owl") nil)
