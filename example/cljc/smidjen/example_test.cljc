(ns smidjen.example-test
  (:require
    #?(:clj
        [smidjen.core :refer [fact facts future-fact future-facts]]
       :cljs
       [smidjen.core :refer-macros [fact facts future-facts future-fact]])))

(fact "about basic string equality"
        "derek" => "clive")

(facts "About number equality"
       (+ 2 2) => 5)

(facts "about multiple number facts interrupted by some other code"
       (comment "test1")
       (+ 1 2) => 4
       (comment "test2")
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
      4 => f
      (future-fact "a future fact"))

(future-facts "top level future fact")

(fact "fact1"
      (+ 7 7) => 42)

;; FIXME this test overrides fact1 test above
(fact "fact1"
      (+ 8 8) => 42)

(facts
  (+ 1 1) => "bob")

(fact "arrows inside let"
      (let [x 1 y 2]
        x => y))

