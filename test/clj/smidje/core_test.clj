(ns smidje.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [smidje.core :as sm :refer [expand-fact expand-future-fact fact facts future-fact future-facts]]
            [cljs.analyzer :refer [resolve-var]]))

(defn stub-gen-sym
  ([vs]
   (let [a (atom vs)]
     (fn g
       ([] (g "G__"))
       ([prefix] (let [f (first @a)] (swap! a rest) (symbol (str prefix f)))))))
  ([] (stub-gen-sym (range))))

(defmacro predictable-macroexpand-1 [form]
  `(with-redefs [clojure.core/gensym (stub-gen-sym)
                 smidje.core/nested-sym (symbol "nested-sym")]
     (print-str (macroexpand-1 ~form))))

(defmacro fact-with-env [env & body]
  (expand-fact env body))

(def test-cases
  {
   "basic fact without string description"
   ['(fact 2 => 2)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 2 2))))]

   "facts synonym"
   ['(facts 3 => 2)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 2 3))))]

   "fact with string description"
   ['(fact "3 is 3" 3 => 3)
    `(deftest ~(symbol "_3-is-3") (let [~'nested-sym 1] (is (= 3 3))))]

   "fact with a string description and two assertions"
   ['(fact "test with two facts"
           (+ 2 2) => 5
           (+ 1 3) => 4)
    `(deftest ~(symbol "test-with-two-facts")
                (let [~'nested-sym 1]
                  (is (clojure.core/= 5 ~'(+ 2 2)))
                  (is (clojure.core/= 4 ~'(+ 1 3)))))]

   "nested fact (i.e. wrapped with nested-sym let binding)"
   ['(fact-with-env {nested-sym 1}
                    "some maths"
                    (+ 4 5) => 9
                    (- 10 8) => 2)
    `(testing "some maths"
                (is (= 9 ~'(+ 4 5)))
                (is (= 2 ~'(- 10 8))))]

   "nested fact with no description"
   ['(fact-with-env {nested-sym 1}
                    (+ 2 2) => 5)
    `(testing "NO_DESCRIPTION" (is (= 5 ~'(+ 2 2))))]

   "fact with assertions separated by another statement"
   ['(fact "a" => "a"
           (prn "something")
           "b" => "b")
    `(deftest ~'G__0
                (let [~'nested-sym 1]
                  (is (= "a" "a"))
                  ~'(prn "something")
                  (is (= "b" "b"))))]

   "the =not=checker"
   ['(fact (+ 1 1) =not=> 3)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (not (= 3 ~'(+ 1 1))))))]

   "ns alias doesn't break things"
   ['(sm/fact (+ 1 1) => 3)
    `(deftest ~'G__0 (let [~'nested-sym 1] (is (= 3 ~'(+ 1 1)))))]

   "future-fact prints out to console"
   ['(future-fact "tbd" (+ 1 1) => 3)
    `(println "WORK TO DO: tbd")]

   "future-facts does the same thing"
   ['(future-facts "tbd" (+ 1 1) => 3)
    `(println "WORK TO DO: tbd")]

   "syms that aren't written by macro aren't qualified"
   ['(fact "f" (= 2 2) => true)
    `(deftest ~'f (let [~'nested-sym 1] (is (= true ~'(= 2 2)))))]

   "if :ns is present in environment, then assumes clojurescript"
   ['(fact-with-env {:ns "cljs"}
                    3 => 1)
    `(cljs.test/deftest ~'G__0 (let [~'nested-sym 1] (cljs.test/is (cljs.core/= 1 3))))]
   })
;; TODO test nested future-facts
;; TODO nesting works at compile time - but what about functions that wrap facts, then being called inside another fact block?

(def expanded
  (->> test-cases
       (map (fn [[test-name [actual expected]]]
              [test-name
               [(predictable-macroexpand-1 actual)
                (print-str expected)]]))
       (into {})))

(deftest fact-macro
  (doseq [[test-name [actual expected]] expanded]
    (testing test-name
      (is (= expected actual)))))

