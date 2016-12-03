(ns smidje.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [smidje.core :as sm :refer [fact facts => =not=> future-fact future-facts]]))

(def expansions
  {;
   ;; basic fact
   '(fact 2 => 2)
   `(testing (is (= 2 2)))

   ;; fact with a string description
   '(fact "3 is 3" 3 => 3)
   `(testing "3 is 3" (is (= 3 3)))

   ;; fact with a string description and two assertions
   '(fact "test with two facts"
          (+ 2 2) => 5
          (+ 1 3) => 4)
   `(testing "test with two facts"
      (is (= 5 (~'+ 2 2)))
      (is (= 4 (~'+ 1 3))))

   ;; fact with assertions separated by another statement
   '(fact "a" => "a"
          (prn "something")
          "b" => "b")
   `(testing
      (is (= "a" "a"))
      ~'(prn "something")
      (is (= "b" "b")))

   ;; nested facts
   '(fact "top level fact"
          (+ 1 1) => 2
          (fact "another fact" 5 => (+ 2 3))
          (fact (str (+ 2 2)) => "4"))
   `(testing "top level fact"
      (is (= 2 ~'(+ 1 1)))
      (testing "another fact"
        (is (= ~'(+ 2 3) 5)))
      (testing
        (is (= "4" ~'(str (+ 2 2))))))

   ;; nested facts with facts synonym
   '(facts "top level fact group"
           (fact (+ 1 1) => 3)
           (facts "more" (str 2) => "2"))
   `(testing "top level fact group"
      (testing (is (= 3 (~'+ 1 1))))
      (testing "more"
        (is (= "2" ~'(str 2)))))

   ;; the =not=checker
   '(fact (+ 1 1) =not=> 3)
   `(testing (is (not (= 3 ~'(+ 1 1)))))

   ; can use alias
   '(sm/facts
      (sm/fact (+ 1 1) => 3))
   `(testing
      (testing (is (= 3 ~'(+ 1 1)))))

   ; future-fact prints out to console
   '(future-fact "tbd" (+ 1 1) => 3)
   `(prn "WORK TO DO: tbd")

   ; future-fact can be nested along with other facts
   '(facts "some-facts"
           (fact 2 => 3)
           (future-fact "future" 3 => 4))
   `(testing "some-facts"
      (testing (is (= 3 2)))
      (prn "WORK TO DO: future"))

   ; future-facts alias can be used
   '(future-facts "some-facts"
                  (fact 2 => 3))
   `(prn "WORK TO DO: some-facts")

   '(facts "facts"
           :a => :b
           (future-facts "blah" 1 2 3))
   `(testing "facts"
      (is (= :b :a))
      (prn "WORK TO DO: blah"))

   })

(def test-cases
  (doall
    (for [[actual expected] expansions]
      [(str expected)
       (str (macroexpand-1 actual))])))

(deftest fact-macro
  (doseq [[expected actual] test-cases]
    (is (= expected actual))))