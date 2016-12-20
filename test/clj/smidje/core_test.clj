(ns smidje.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [smidje.core :as sm :refer [fact facts =not=> => future-fact future-facts]]
            [clojure.walk :refer [macroexpand-all]]
            [clojure.test :as t]))


(defn stub-gen-sym
  ([vs]
   (let [a (atom vs)]
     (fn g
       ([] (g "G__"))
       ([prefix] (let [f (first @a)] (swap! a rest) (symbol (str prefix f)))))))
  ([] (stub-gen-sym (range))))

(defn predictable-macroexpand-all [form]
  (with-redefs [clojure.core/gensym (stub-gen-sym)
                smidje.core/clj-ns {:core 'c :test 't}]
    (print-str (macroexpand-all form))))

;
(def expansions
  {
   "basic fact without string description"
   ['(fact 2 => 2)
    `(t/deftest ~'G__0 (t/is (c/= 2 2)))]

   "facts synonym"
   ['(facts 3 => 2)
    `(t/deftest ~'G__0 (t/is (c/= 2 3)))]

   "fact with string description"
   ['(fact "3 is 3" 3 => 3)
    `(t/deftest ~(symbol "3 is 3") (t/is (c/= 3 3)))]

   "fact with a string description and two assertions"
   ['(fact "test with two facts"
           (+ 2 2) => 5
           (+ 1 3) => 4)
    `(t/deftest ~(symbol "test with two facts")
       (t/is (c/= 5 ~'(+ 2 2)))
       (t/is (c/= 4 ~'(+ 1 3))))]

   "nested facts with descriptions"
   ['(facts "some facts"
            (fact "a=b" "a" => "b")
            (fact "c=d" "c" => "d"))
    `(t/deftest ~(symbol "some facts")
       (t/testing "a=b" (t/is (c/= "b" "a")))
       (t/testing "c=d" (t/is (c/= "d" "c"))))]

   "nested facts with no descriptions"
   ['(facts "some facts"
            (fact true => true)
            (fact false => true))
    `(t/deftest ~(symbol "some facts")
       (t/testing "NO_DESCRIPTION" (t/is (c/= true true)))
       (t/testing "NO_DESCRIPTION" (t/is (c/= true false))))]

   "fact with assertions separated by another statement"
   ['(fact "a" => "a"
           (prn "something")
           "b" => "b")
    `(t/deftest ~'G__0
       (t/is (c/= "a" "a"))
       ~'(prn "something")
       (t/is (c/= "b" "b")))]

   "the =not=checker"
   ['(fact (+ 1 1) =not=> 3)
    `(t/deftest ~'G__0 (t/is (c/not (c/= 3 ~'(+ 1 1)))))]

   "ns alias doesn't break things"
   ['(sm/fact (+ 1 1) => 3)
    `(t/deftest ~'G__0 (t/is (c/= 3 ~'(+ 1 1))))]

   "future-fact prints out to console"
   ['(future-fact "tbd" (+ 1 1) => 3)
    `(c/println "WORK TO DO: tbd")]

   "future-facts does the same thing"
   ['(future-facts "tbd" (+ 1 1) => 3)
    `(c/println "WORK TO DO: tbd")]

   ;; TODO test nested future-facts
   ;; TODO TEST that let binding doesn't mess stuff up
   })


(def expansions2
  (->> expansions
       (map (fn [[test-name [actual expected]]]
              [test-name
               [(predictable-macroexpand-all actual)
                (predictable-macroexpand-all expected)]]))
       (into {})))

(deftest fact-macro
  (doseq [[test-name [actual expected]] expansions2]
    (testing test-name
      (is (= expected actual)))))

;; TODO write tests for cljs expansions



