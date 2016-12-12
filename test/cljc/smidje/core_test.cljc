(ns smidje.core-test
  #?(:clj
     (:require
       [clojure.test :as test]
       [smidje.core :as sm :refer [fact facts => =not=> future-fact future-facts]])
     :cljs
     (:require
       [cljs.test :as test]
       [cljs.analyzer :as ana]
       [smidje.core :as sm :refer [fact facts => =not=> future-fact future-facts] :include-macros true])))

(def testing #?(:clj 'clojure.test/testing :cljs 'cljs.test/testing))
(def is #?(:clj 'clojure.test/is :cljs 'cljs.test/is))
(def deftest #?(:clj 'clojure.test/deftest :cljs 'cljs.test/deftest))


(def expansions
  {
   "basic fact"
   [(macroexpand-1 '(fact 2 => 2))
    `(~testing (~is (= 2 2)))]

   "facts synonym"
   [(macroexpand-1 '(facts 3 => 2))
    `(~testing (~is (= 2 3)))]

   "fact with string description"
   [(macroexpand-1 '(fact "3 is 3" 3 => 3))
    `(~testing "3 is 3" (~is (= 3 3)))]

   "fact with a string description and two assertions"
   [(macroexpand-1 '(fact "test with two facts"
                          (+ 2 2) => 5
                          (+ 1 3) => 4))
    `(~testing "test with two facts"
       (~is (= 5 (~'+ 2 2)))
       (~is (= 4 (~'+ 1 3))))]

   "fact with assertions separated by another statement"
   [(macroexpand-1 '(fact "a" => "a"
                          (prn "something")
                          "b" => "b"))
    `(~testing
       (~is (= "a" "a"))
       ~'(prn "something")
       (~is (= "b" "b")))]

   "the =not=checker"
   [(macroexpand-1 '(fact (+ 1 1) =not=> 3))
    `(~testing (~is (not (= 3 (~'+ 1 1)))))]

   "ns alias doesn't break things"
   [(macroexpand-1 '(sm/fact (+ 1 1) => 3))
    `(~testing (~is (= 3 ~'(+ 1 1))))]

   "future-fact prints out to console"
   [(macroexpand-1 '(future-fact "tbd" (+ 1 1) => 3))
    `(prn "WORK TO DO: tbd")]

   "future-facts does the same thing"
   [(macroexpand-1 '(future-facts "tbd" (+ 1 1) => 3))
    `(prn "WORK TO DO: tbd")]
   })

(test/deftest fact-macro
  (doseq [[test-name [actual expected]] expansions]
    (test/testing test-name
      (test/is (= (print-str expected)
                  (print-str actual))))))

(fact-macro)