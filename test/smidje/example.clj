(ns smidje.example
  (:require
    [clojure.test :refer [deftest]]
    [smidje.core :refer [fact facts =>]]))

(comment
  (deftest first-fact
    (fact "a fact"
          (+ 2 2) => 6
          (fact (+ 2 2) => 5
                (+ 2 4) => 5))))
