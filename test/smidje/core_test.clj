(ns smidje.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [smidje.core :as sm :refer [fact facts => =not=>]]))

(def expansions
  {;
   ;; basic fact
   '(fact 2 => 2)
   '(clojure.test/testing (clojure.test/is (clojure.core/= 2 2)))

   ;; fact with a string description
   '(fact "3 is 3" 3 => 3)
   '(clojure.test/testing "3 is 3" (clojure.test/is (clojure.core/= 3 3)))

   ;; fact with a string description and two assertions
   '(fact "test with two facts"
          (+ 2 2) => 5
          (+ 1 3) => 4)
   '(clojure.test/testing "test with two facts"
      (clojure.test/is (clojure.core/= 5 (+ 2 2)))
      (clojure.test/is (clojure.core/= 4 (+ 1 3))))

   ;; fact with assertions separated by another statement
   '(fact "a" => "a"
          (prn "something")
          "b" => "b")
   '(clojure.test/testing
      (clojure.test/is (clojure.core/= "a" "a"))
      (prn "something")
      (clojure.test/is (clojure.core/= "b" "b")))

   ;; nested facts
   '(fact "top level fact"
          (+ 1 1) => 2
          (fact "another fact" 5 => (+ 2 3))
          (fact (str (+ 2 2)) => "4"))
   '(clojure.test/testing "top level fact"
      (clojure.test/is (clojure.core/= 2 (+ 1 1)))
      (clojure.test/testing "another fact"
        (clojure.test/is (clojure.core/= (+ 2 3) 5)))
      (clojure.test/testing
        (clojure.test/is (clojure.core/= "4" (str (+ 2 2))))))

   ;; nested facts with facts synonym
   '(facts "top level fact group"
           (fact (+ 1 1) => 3)
           (facts "more" (str 2) => "2"))
   '(clojure.test/testing "top level fact group"
      (clojure.test/testing (clojure.test/is (clojure.core/= 3 (+ 1 1))))
      (clojure.test/testing "more"
        (clojure.test/is (clojure.core/= "2" (str 2)))))

   ;; the =not=checker
   '(fact (+ 1 1) =not=> 3)
   '(clojure.test/testing (clojure.test/is (clojure.core/not (clojure.core/= 3 (+ 1 1)))))

   ; can use alias
   '(sm/facts
      (sm/fact (+ 1 1) => 3))
   '(clojure.test/testing
      (clojure.test/testing (clojure.test/is (clojure.core/= 3 (+ 1 1)))))

   })

(def test-cases
  (doall
    (for [[actual expected] expansions]
      [(str expected)
       (str (macroexpand-1 actual))])))

(deftest fact-macro
  (doseq [[expected actual] test-cases]
    (is (= expected actual))))