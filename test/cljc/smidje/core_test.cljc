(ns smidje.core-test
  #?(:clj
     (:require [clojure.test :as test]
               [smidje.core :as sm :refer [fact facts => =not=> future-fact future-facts]])
     :cljs
     (:require [cljs.test :as test :include-macros true]
               [smidje.core :as sm :refer [fact facts => =not=> future-fact future-facts] :include-macros true])))

(def testing #?(:clj 'clojure.test/testing :cljs 'cljs.test/testing))
(def is #?(:clj 'clojure.test/is :cljs 'cljs.test/is))

(def expansions
  {;
   ; basic fact
   (macroexpand-1 '(fact 2 => 2))
   `(~testing (~is (= 2 2)))

   ;;; fact with a string description
   (macroexpand-1 '(fact "3 is 3" 3 => 3))
   `(~testing "3 is 3" (~is (= 3 3)))
   ;
   ;;; fact with a string description and two assertions
   (macroexpand-1 '(fact "test with two facts"
                         (+ 2 2) => 5
                         (+ 1 3) => 4))
   `(~testing "test with two facts"
      (~is (= 5 (~'+ 2 2)))
      (~is (= 4 (~'+ 1 3))))

   ;; fact with assertions separated by another statement
   (macroexpand-1 '(fact "a" => "a"
                         (prn "something")
                         "b" => "b"))
   `(~testing
      (~is (= "a" "a"))
      ~'(prn "something")
      (~is (= "b" "b")))

   ;; nested facts
   (macroexpand-1 '(fact "top level fact"
                         (+ 1 1) => 2
                         (fact "another fact" 5 => (+ 2 3))
                         (fact (str (+ 2 2)) => "4")))
   `(~testing "top level fact"
      (~is (= 2 ~'(+ 1 1)))
      (~testing "another fact"
        (~is (= ~'(+ 2 3) 5)))
      (~testing
        (~is (= "4" ~'(str (+ 2 2))))))

   ;; nested facts with facts synonym
   (macroexpand-1 '(facts "top level fact group"
                          (fact (+ 1 1) => 3)
                          (facts "more" (str 2) => "2")))
   `(~testing "top level fact group"
      (~testing (~is (= 3 (~'+ 1 1))))
      (~testing "more"
        (~is (= "2" ~'(str 2)))))

   ;; the =not=checker
   (macroexpand-1
     '(fact (+ 1 1) =not=> 3))
   `(~testing (~is (not (= 3 ~'(+ 1 1)))))

   ; can use alias
   (macroexpand-1 '(sm/facts
                     (sm/fact (+ 1 1) => 3)))
   `(~testing
      (~testing (~is (= 3 ~'(+ 1 1)))))

   ; future-fact prints out to console
   (macroexpand-1 '(future-fact "tbd" (+ 1 1) => 3))
   `(prn "WORK TO DO: tbd")

   ; future-fact can be nested along with other facts
   (macroexpand-1 '(facts "some-facts"
                          (fact 2 => 3)
                          (future-fact "future" 3 => 4)))
   `(~testing "some-facts"
      (~testing (~is (= 3 2)))
      (prn "WORK TO DO: future"))

   ; future-facts alias can be used
   (macroexpand-1 '(future-facts "some-facts"
                                 (fact 2 => 3)))
   `(prn "WORK TO DO: some-facts")

   (macroexpand-1
     '(facts "facts"
             :a => :b
             (future-facts "blah" 1 2 3)))
   `(~testing "facts"
             (~is (= :b :a))
             (prn "WORK TO DO: blah"))

   })

(test/deftest fact-macro
  (doseq [[actual expected] expansions]
    (test/is (= (str expected) (str actual)))))