(ns smidje.example-test
  (:require
    #?(:clj [smidje.core :refer [fact facts => =not=>]]
       :cljs [smidje.core :refer [=> =not=>] :refer-macros [fact facts]])))

;(fact "about basic string equality"
;      "derek" => "clive")
;
;(facts "About number equality"
;       (+ 2 2) => 5)
;
;(facts "about multiple number facts, interrupted by some other code"
;       (prn "test1")
;       (+ 1 2) => 4
;       (prn "test2")
;       5 => (- 7 3))
;
;(facts
;  (+ 1 1) => "bob")
