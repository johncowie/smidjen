(ns smidje.example-test
  (:require
    #?(:clj
        [smidje.core :refer [fact facts]]
       :cljs
       [smidje.core :refer-macros [fact facts future-facts future-fact]])))

(fact "about basic string equality"
        "derek" => "clive")

(facts "About number equality"
       (+ 2 2) => 5)

(facts "about multiple number facts interrupted by some other code"
       (prn "test1")
       (+ 1 2) => 4
       (prn "test2")
       5 => (- 7 3))

(fact "3 is a number at the beginning of a test"
       3 => 4)

(fact "can use predicate"
      5 => even?)

(fact "can use custom predicate"
      4 => #(= % 5)
      (fact "full expresion"
            5 => (fn [v] (= 6 v))))

(def f 6)

(fact "can test against symbol"
      4 => 6)

(facts
  (+ 1 1) => "bob")

