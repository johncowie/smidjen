(ns smidje.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [smidje.core :as sm :refer [expand-fact expand-future-fact fact facts future-fact future-facts]]
            [clojure.walk :refer [macroexpand-all]]
            [cljs.analyzer :refer [resolve-var]]))

(defn stub-gen-sym
  ([vs]
   (let [a (atom vs)]
     (fn g
       ([] (g "G__"))
       ([prefix] (let [f (first @a)] (swap! a rest) (symbol (str prefix f)))))))
  ([] (stub-gen-sym (range))))

(defmacro predictable-macroexpand-all [form]
  `(with-redefs [clojure.core/gensym (stub-gen-sym)
                 smidje.core/clj-ns {:core 'c :test 't}
                 smidje.core/cljs-ns {:core 'jsc :test 'jst}
                 smidje.core/nested-sym (symbol "nested-sym")
                 cljs.analyzer/resolve-var (fn [~'e ~'s]
                                             (get {"=>"     {:name "smidje.core/=>"}
                                                   "=not=>" {:name "smidje.core/=not=>"}} (str ~'s)))]
     (print-str (macroexpand-all ~form))))

(defmacro fact-with-env [env & body]
  (expand-fact env body))

(def test-cases
  {
   "basic fact without string description"
   ['(fact 2 => 2)
    `(t/deftest ~'G__0 (let* [~'nested-sym 1] (t/is (c/= 2 2))))]

   "facts synonym"
   ['(facts 3 => 2)
    `(t/deftest ~'G__0 (let* [~'nested-sym 1] (t/is (c/= 2 3))))]

   "fact with string description"
   ['(fact "3 is 3" 3 => 3)
    `(t/deftest ~(symbol "3 is 3") (let* [~'nested-sym 1] (t/is (c/= 3 3))))]

   "fact with a string description and two assertions"
   ['(fact "test with two facts"
           (+ 2 2) => 5
           (+ 1 3) => 4)
    `(t/deftest ~(symbol "test with two facts")
                (let* [~'nested-sym 1]
                  (t/is (c/= 5 ~'(+ 2 2)))
                  (t/is (c/= 4 ~'(+ 1 3)))))]

   "nested fact (i.e. wrapped with nested-sym let binding)"
   ['(fact-with-env {nested-sym 1}
                    "some maths"
                    (+ 4 5) => 9
                    (- 10 8) => 2)
    `(t/testing "some maths"
                (t/is (c/= 9 ~'(+ 4 5)))
                (t/is (c/= 2 ~'(- 10 8))))]

   "nested fact with no description"
   ['(fact-with-env {nested-sym 1}
                    (+ 2 2) => 5)
    `(t/testing "NO_DESCRIPTION" (t/is (c/= 5 ~'(+ 2 2))))]

   "fact with assertions separated by another statement"
   ['(fact "a" => "a"
           (prn "something")
           "b" => "b")
    `(t/deftest ~'G__0
                (let* [~'nested-sym 1]
                  (t/is (c/= "a" "a"))
                  ~'(prn "something")
                  (t/is (c/= "b" "b"))))]

   "the =not=checker"
   ['(fact (+ 1 1) =not=> 3)
    `(t/deftest ~'G__0 (let* [~'nested-sym 1] (t/is (c/not (c/= 3 ~'(+ 1 1))))))]

   "ns alias doesn't break things"
   ['(sm/fact (+ 1 1) => 3)
    `(t/deftest ~'G__0 (let* [~'nested-sym 1] (t/is (c/= 3 ~'(+ 1 1)))))]

   "future-fact prints out to console"
   ['(future-fact "tbd" (+ 1 1) => 3)
    `(c/println "WORK TO DO: tbd")]

   "future-facts does the same thing"
   ['(future-facts "tbd" (+ 1 1) => 3)
    `(c/println "WORK TO DO: tbd")]

   "if :ns is present in environment, then assumes clojurescript"
   ['(fact-with-env {:ns "cljs"}
                    3 => 1)
    `(jst/deftest ~'G__0 (let* [~'nested-sym 1] (jst/is (jsc/= 1 3))))]})
;; TODO test nested future-facts
;; TODO nesting works at compile time - but what about functions that wrap facts, then being called inside another fact block?

(def expanded
  (->> test-cases
       (map (fn [[test-name [actual expected]]]
              [test-name
               [(predictable-macroexpand-all actual)
                (predictable-macroexpand-all expected)]]))
       (into {})))

(deftest fact-macro
  (doseq [[test-name [actual expected]] expanded]
    (testing test-name
      (is (= expected actual)))))
;; Midje aliasing
;; YES FOR FACTS
;; *NOT* FOR ARROWS - could massively simplify things by not trying to resolve them


